package org.senkbeil.sitegen

import java.nio.file.{Files, Path, Paths}

import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand.ResetType
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a publisher of content based on a configuration.
 *
 * @param config The configuration to use when publishing files
 */
class Publisher(private val config: Config) extends Runnable {
  /** Logger for this class. */
  private lazy val logger = new Logger(this.getClass)

  /**
   * Publishes the content in the output directory.
   */
  def run(): Unit = logger.time(Logger.Level.Info, "Publish finished after ") {
    val outputDirPath = Paths.get(config.generate.outputDir())
    val remoteName = config.publish.remoteName()
    val remoteBranch = config.publish.remoteBranch()

    val repoPath = {
      val rootPath = Paths.get(".").toAbsolutePath
      val git = gitForPath(rootPath)
      copyRepoToCache(git.getRepository, force = false)
    }

    logger.trace("Loading configuration data")
    val git = gitForPath(repoPath)
    val authorName = config.publish.authorName.toOption.orElse(
      Option(git.getRepository.getConfig.getString("user", null, "name"))
    ).getOrElse(
      throw new IllegalStateException("No Git author name available!")
    )
    val authorEmail = config.publish.authorEmail.toOption.orElse(
      Option(git.getRepository.getConfig.getString("user", null, "email"))
    ).getOrElse(
      throw new IllegalStateException("No Git author email available!")
    )

    logger.trace(s"Rebasing repository found at $repoPath")
    (() => {
      val result = prepareBranch(git, remoteName, remoteBranch)

      result.foreach(commitId => logger.verbose(s"Rebased to $commitId"))
      result.failed.foreach(logger.error)
    })()

    logger.trace("Wiping old branch contents")
    Files.newDirectoryStream(repoPath).asScala
      .filterNot(_ == repoPath.resolve(".git"))
      .map(_.toFile)
      .foreach(FileUtils.forceDelete)

    logger.verbose(s"Copying contents from $outputDirPath to $repoPath")
    FileUtils.copyDirectory(outputDirPath.toFile, repoPath.toFile)

    logger.trace("Adding changes")
    git.add().addFilepattern(".").call() // To add untracked files

    logger.info(s"Committing changes as $authorName <$authorEmail>")
    val dateString = {
      val format = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      format.format(new java.util.Date())
    }
    git.commit()
      .setAll(true) // To add "deleted" files
      .setAuthor(authorName, authorEmail)
      .setCommitter(authorName, authorEmail)
      .setMessage(s"New publish on $dateString")
      .call().disposeBody()

    logger.info(s"Pushing to $remoteName/$remoteBranch")
    git.push().setRemote(remoteName).call()
  }

  /**
   * Copies the repository to the cache directory.
   *
   * @param repository The repository to copy
   * @param force If true, will force copying instead of ignoring if already
   *              cached
   * @return The path to the cached directory containing the repository
   */
  private def copyRepoToCache(repository: Repository, force: Boolean): Path = {
    val cacheRootPath = Paths.get(config.publish.cacheDir())
    val alreadyExists = Files.exists(cacheRootPath)

    // If for some reason a file exists as our cache root, fail loudly
    if (alreadyExists && !Files.isDirectory(cacheRootPath)) {
      throw new IllegalStateException(s"Publish cache $cacheRootPath is file!")
    }

    // Create the directory if it doesn't exist
    if (!alreadyExists) {
      logger.verbose(s"Creating $cacheRootPath for first time")
      Files.createDirectories(cacheRootPath)
    }

    // If not already exists or being forced, copy contents to cache
    val workTreePath = repository.getWorkTree.toPath.toAbsolutePath.normalize()
    val destinationPath = cacheRootPath.resolve(workTreePath.getFileName)
    if (!Files.exists(destinationPath) || force) {
      FileUtils.deleteDirectory(destinationPath.toFile)
      Files.createDirectories(destinationPath)

      logger.info(s"Copying $workTreePath to $destinationPath")
      FileUtils.copyDirectory(workTreePath.toFile, destinationPath.toFile)
    } else {
      logger.verbose(s"$destinationPath already exists, so not copying!")
    }

    destinationPath
  }

  /**
   * Attempts to reset the repository branch to a clean state.
   *
   * @param git The git instance whose repository to rebase
   * @param remoteName The name of the remote repo (e.g. origin) whose commit
   *                   revision history to use when rebasing
   * @param branchName The name of the branch to rebase
   * @return Success containing the top commit id after rebasing, or a Failure
   */
  private def prepareBranch(
    git: Git,
    remoteName: String = "origin",
    branchName: String = "gh-pages"
  ): Try[String] = {
    val result = Try({
      val repo = git.getRepository
      val repoPath = repo.getWorkTree.toPath.normalize()
      val remoteBranchName = s"$remoteName/$branchName"

      // Reset any pending changes in the copy
      logger.trace(s"Clearing any changes in $repoPath")
      git.reset()
        .setMode(ResetType.HARD)
        .call()

      // Fetch the latest from our remote
      logger.trace(s"Fetching latest from $remoteName")
      git.fetch().setCheckFetchedObjects(true).setRemote(remoteName).call()

      // See if the desired branch already exists
      val branchExists = git
        .branchList()
        .call().asScala
        .exists(_.getName == branchName)

      if (!branchExists) {
        logger.trace(s"Creating $branchName to track $remoteBranchName")
        git.branchCreate()
          .setName(branchName)
          .setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM)
          .setStartPoint(remoteBranchName)
          .setForce(true)
          .call()
      }

      logger.trace(s"Checking out $branchName")
      git.checkout().setName(branchName).call()

      // Ensure we have the latest from the remote repo
      logger.trace(s"Pulling latest from $remoteBranchName")
      git.pull()
        .setRemote(remoteName)
        .setRemoteBranchName(branchName)
        .call()

      // Find the top commit of the branch
      val commits = git.log().setMaxCount(1).call().asScala.toSeq
      commits.headOption.map(_.getName).getOrElse(
        throw new IllegalStateException(s"$branchName has no commits!")
      )
    })

    result
  }

  /**
   * Creates a git object for a local git repo containing the specified path.
   *
   * @param path The path managed by a git repo
   * @return The Git instance
   */
  private def gitForPath(path: Path): Git = {
    val builder = new FileRepositoryBuilder
    val repository = builder.readEnvironment().findGitDir(path.toFile).build()

    new Git(repository)
  }
}
