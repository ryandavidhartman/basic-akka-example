package part1recap

object ThreadModelLimitations2 extends App {

  // DR #2 - delegating a task to a thread is hard.
  // How can we given information (i.e. signal) a running thread?
  // Say we have some running thread.  How can we pass a runnable to it?

  // In this example use a global var to hold the state of the runnable.
  // this is sort of gross.


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

/*
How do you solve the case for other types of signals?
Handle multiple background tasks?
What if you need to know who set the signal?
What if there is an error or crash?
 */
