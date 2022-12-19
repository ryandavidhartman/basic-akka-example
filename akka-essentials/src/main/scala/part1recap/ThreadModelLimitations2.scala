package part1recap

object ThreadModelLimitations2 extends App {

  // DR #2 - delegating a task to a thread

  var task: Runnable = null

  val runningThread: Thread = new Thread(() => {
    while (true) {
      while (task == null) {
        runningThread.synchronized {
          println("[background] waiting for a task")
          runningThread.wait()
        }
      }

      task.synchronized {
        println("[background] I have a task!")
        task.run()
        task = null
      }
    }
  })

  def delegateToBackgroundThread(r: Runnable): Unit = {
    if (task == null) {
      task = r
      runningThread.synchronized {
        runningThread.notify()
      }
    }
  }

  def demoBackgroundDelegation(): Unit = {
    runningThread.start()
    Thread.sleep(1000)
    delegateToBackgroundThread(() => println("I'm running from another thread"))
    Thread.sleep(1000)
    delegateToBackgroundThread(() => println("This should run in the background again"))
  }

  demoBackgroundDelegation()

}
