import akka.actor._
import akka.routing.RoundRobinRouter
import akka.util.Duration
import akka.util.duration_

sealed trait StoryMessage
case class Process(topic: String, htmlBody: String) extends StoryMessage
case class Result(topic: String, topicItems: Array[String], strippedBody: String, bodyItems: Array[String]) extends StoryMessage

//Worker bee responsible for taking an article, stripping out the HTMl tags and tokeninzing the data
class StoryTokenWorker extends Actor {
	def receive = {
		case Process(topic, htmlBody) =>
			sender ! Result(topic, tokenizeTopic(topic), stripHtml(htmlBody), tokenizeBody(stripHtml(htmlBody)))
	}

	def stripHtml(text : String) = { body.replaceAll("""<(?!\/?a(?=>|\s.*>))\/?.*?>""", "") }

	def tokenizeTopic(text: String) = { text }
	
	def tokenizeBody(text: String) = {text}
}

//Worker bee responsible for taking a tokenize story and matching it against others in the database/memory?
class StoryMatchingWorker extends Actor {
	
}

//First in the pipeline, receives a newly identified story and begins working it
class StoryMaster(numberOfWorkers: Int, listener: ActorRef) extends Actor {
	val workerRouter = content.actorOf(Props[StoryTokenWorker].withRouter(RoundRobinRouter(numberOfWorkers)), name = "storyTokenWorkerRouter")

	def receive = {
		case Process =>
			workerRouter ! Process
		case Result => 
			//TODO: aggregate the results...  DB or in memory? algos for matching.
	}
}

//Class which listens for a story to be fully analyzed and puts the results in the DB
class Listener extends Actor {
}
