package com.soulmedia.aggrigator

import akka.actor._
import akka.routing.RoundRobinRouter
import scala.io._
//import akka.util.duration._

<<<<<<< HEAD
import opennlp.tools.sentdetect._
import opennlp.tools.parser._
=======
object WordType extends Enumeration {
  val Noun, Verb, Adjective = Value
}
>>>>>>> Old change.

sealed trait StoryMessage
case class Process(topic: String, htmlBody: String) extends StoryMessage
case class FullStory(topic: String, cleanBody: String) extends StoryMessage
case class SentenceStory(topic: String, sentences: String[]) extends StoryMessage
case class ParsedStory(topic: Parse, parsedSentences: List[Parse]) extends StoryMessage
case class Result(topic: String, topicItems: Array[String], strippedBody: String, bodyItems: Array[String]) extends StoryMessage
case class FinalResult(result:String) extends StoryMessage

//Worker bee responsible for taking an article, stripping out the HTMl tags and tokeninzing the data
class StoryTokenWorker extends Actor {
<<<<<<< HEAD
=======
  val wordMap = Source.fromFile("infl.txt").map( wordDef => (wordDef.substring(0, wordDef.indexOf(" ")),
    wordDef.substring(wordDef.indexOf(" "), wordDef.indexOf(":")).replaceAll("?", "") match {
      case "N" => WordType.Noun
      case "A" => WordType.Adjective
      case "V" => WordType.Verb
    })) toMap

  val punctRegex = """!|.|\ ?""".r
>>>>>>> Old change.

	def receive = {
		case Process(topic, htmlBody) =>
            println("StoryTokenWorker: ")
            val setenceAnalyzer = context.actorOf(Props[SentenceAnalysisWorker], name = "sentenceAnalyzer")
			
            setenceAnalyzer ! FullStory(stripHtml(topic), stripHtml(htmlBody))
	}

	def stripHtml(text : String) = { text.replaceAll("""<(?!\/?a(?=>|\s.*>))\/?.*?>""", "") }

<<<<<<< HEAD
=======
	def tokenizeTopic(text: String) = { text.split(" ").map(word => word.indexOf("")) }
	
	def tokenizeBody(text: String) = { text.split(" ") }
>>>>>>> Old change.
}

class StoryAnalysisWorker extends Actor {
    def receive = {
        case ParsedStory(topic, parsedSentences) =>
            val actorNoun = getActorNoun(topic)
            val actionVerb = getActionVerb(topic)
            val targetNoun = getTargetNoun(topic)
            
            println("StoryAnalysisWorker: Got here!")
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
                
                val parser = ParserFactory.create(parseModel)
                
                val parsedSentence = ParserTool.parseLine(sentence, parser, 1)
                
                val debugBuffer = StringBuffer()
                
                debugBuffer.append("SentenceParsingWorker: Story structure - \n")
                
                parsedSentence[0].show(debugBuffer)
                
                println(debugBuffer.toString())
                
                parsedSentence[0]
            })  

            val topicStructure = {
                val stringIS = InputStream(ByteArray(topic.getBytes()))
            
                try {
                    val parseModel = ParserModel(stringIS)
                } catch {
                    case e:IOException =>
                        println("Could not read story.")
                } finally {
                    if(stringIS) stringIS.close()
                }
                
                val parser = ParserFactory.create(parseModel)
                
                val parsedSentence = ParserTool.parseLine(topic, parser, 1)
                
                val debugBuffer = StringBuffer()
                
                debugBuffer.append("SentenceParsingWorker: Topic structure - \n")
                
                parsedSentence[0].show(debugBuffer)
                
                println(debugBuffer.toString())
                
                parsedSentence[0]
            }
            
            val storyAnalyzer = context.actorOf(Props[StoryAnalysisWorker], name = "storyAnalyzer")
            
            storyAnalyzer ! ParsedStory(topicStructure, sentenceStructures)
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
            
            println(s"SentenceAnalysisWorker: Found ${sentences.length} sentences.")
            
            val sentenceParser = context.actorOf(Props[SentenceParsingWorker], name = "sentenceParser")
            
            sentenceParser ! SentenceStory(topic, sentences)
            
        }
    }
}

class Listener extends Actor {
    def receive = {
        case FinalResult(result) =>
            println(result)
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
