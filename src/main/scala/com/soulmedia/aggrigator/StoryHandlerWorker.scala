import akka.actor._
import akka.routing.RoundRobinRouter
//import akka.util.duration._

import opennlp.tools.sentdetect._
import opennlp.tools.parser._

sealed trait StoryMessage
case class Process(topic: String, htmlBody: String) extends StoryMessage
case class FullStory(topic: String, cleanBody: String) extends StoryMessage
case class SentenceStory(topic: String, sentences: String[]) extends StoryMessage
case class ParsedStory(topic: Parse, parsedSentences: List[Parse]) extends StoryMessage
case class Result(topic: String, topicItems: Array[String], strippedBody: String, bodyItems: Array[String]) extends StoryMessage

//Worker bee responsible for taking an article, stripping out the HTMl tags and tokeninzing the data
class StoryTokenWorker extends Actor {

	def receive = {
		case Process(topic, htmlBody) =>
            val setenceAnalyzer = system.actorOf(Props[SentenceAnalysisWorker], name = "sentenceAnalyzer")
			
            setenceAnalyzer ! FullStory(stripHtml(topic), stripHtml(htmlBody))
	}

	def stripHtml(text : String) = { text.replaceAll("""<(?!\/?a(?=>|\s.*>))\/?.*?>""", "") }

}

class SentenceAnalysisWorker extends Actor {
    def receive = {
        case ParsedStory(topic, parsedSentences) =>
            val actorNoun = getActorNoun(topic)
            val actionVerb = getActionVerb(topic)
            val targetNoun = getTargetNoun(topic)
    }
    
    def getActorNoun(input: Parse) = {}
    
    def getActionVerb(input: Parse) = {}
    
    def getTargetNoun(input: Parse) = {}
}

class SentenceParsingWorker extends Actor {
    def receive = {
        case SentenceStory(topic, sentences) =>
            val sentenceStructures = sentences.map((sentence) => {
                val stringIS = InputStream(ByteArray(sentence.getBytes()))
            
                try {
                    val parseModel = ParserModel(stringIS)
                } catch {
                    case e:IOException =>
                        println("Could not read story.")
                } finally {
                    if(stringIS) stringIS.close()
                }
                
                val parser = PraserFactory.create(parseModel)
                
                val parsedSentence = ParserTool.parseLine(sentence, parser, 1)
                
                parsedSentence[0]
            })
            
            
            
    }
}

class SentenceAnalysisWorker extends Actor {
    def receive = {
        case FullStory(topic, cleanBody) => {
            //Load a sentence model
            val stringIS = InputStream(ByteArray(cleanBody.getBytes()))
            
            try {
                val sentModel = SentenceModel(stringIS)
            } catch {
                case e:IOException =>
                    println("Could not read story.")
            } finally {
                if(stringIS) stringIS.close()
            }
            
            //Create a sentence detector from the model
            val sentenceDetector = new SentenceDetectorME(model)
            
            val sentences = sentenceDetector.sentDetect(cleanBody)
            
            val sentenceParser = system.actorOf(Props[sentenceParsingWorker], name = "sentenceParser")
            
            sentenceParser ! SentenceStory(topic, sentences)
            
        }
    }
}

//First in the pipeline, receives a newly identified story and begins working it
class StoryMaster(numberOfWorkers: Int, listener: ActorRef) extends Actor {
	val workerRouter = context.actorOf(Props[StoryTokenWorker].withRouter(RoundRobinRouter(numberOfWorkers)), name = "storyTokenWorkerRouter")

	def receive = {
		case Process =>
			workerRouter ! Process
		case Result => 
			//TODO: aggregate the results...  DB or in memory? algos for matching.
	}
}
