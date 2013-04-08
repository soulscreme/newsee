package com.soulmedia.aggrigator

import akka.actor._

object ActorSystemTestBed {
    def main(args: Array[String]) = {
        println("***********************************************************")
        println("Newsee Actor System Tester")
        println("***********************************************************")

        val actorSystem = ActorSystem()
        val listener = actorSystem.actorOf(Props[Listener])
        val masterActor = actorSystem.actorOf(Props(new StoryMaster(3, listener))
        
        val topic = "Kim Jong-un invades China"
        val story = "Korean dictator Kim Jong-un announced today that he has start a full-sclae invasion of the Chinese mainland.  This marks the first aggresive action from North Korea in 50 years."
        
        masterActor ! Process(topic, story)
      }
    }