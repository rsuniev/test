/*
 * Copyright 2008 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */
package bootstrap.liftweb

import _root_.java.util.Locale

import _root_.net.liftweb.common.{Box,Empty,Full}
import _root_.net.liftweb.util.{ClassHelpers, LogBoot, Log, Slf4jLogBoot}
import _root_.net.liftweb.http._
import _root_.net.liftweb.http.provider._
import _root_.net.liftweb.sitemap._
import _root_.net.liftweb.sitemap.Loc._
import com.mycompany.{ComponentRegistry}
import com.mycompany.model._
import com.mycompany.model.{Model}
import S.?
import java.io.File
import scala.collection.jcl.{Buffer}

/**
  * A class that's instantiated early and run.  It allows the application
  * to modify lift's environment
  * 	// val regAndDescription = for (row <- results; pair = row.reg + row.capCode) yield pair
	// Full(PlainTextResponse( "Found codes " + regAndDescription.mkString("\n") +"!"))
  */
class Boot {
    Slf4jLogBoot.enable()
    
    LiftRules.dispatch.append {
      case Req(List("resty", reg), "", GetRequest) => () => showResponse(reg)      
      case r @ Req("api" :: "upload" :: Nil, _, PostRequest) => () => parseFile(r)
    }
    
    def parseFile(req: Req) : Box[LiftResponse] = {                  
      val queryName: String = req.param("query") openOr "default"            
      //TODO refactor
      /*for(f <- req.uploadedFiles) {
        f match{
            case holder: FileParamHolder => parseFile(holder.fileStream)            
        }
      }*/
      for (f <- req.uploadedFiles; ar = parseFile(f.fileStream)){}
      Full(PlainTextResponse( "result" ))
    } 
      
    def parseFile(stream : java.io.InputStream) = {      
      for (row <- scala.io.Source.fromInputStream(stream).getLines; ar = row.split(",")) {
	 println(ar(0) + "<-->" + ar(1)) 
      }
    }
    
    def processFileRow(row: String) = {
      
    }
    
    def showResponse(reg: String): Box[LiftResponse] = {	
	var response = "";
	val results = Model.createNamedQuery[HPIResult]("HPIResult.findByReg", "reg" -> reg).getResultList.toList match {
	  case hpiResult :: Nil => (response = hpiResult.capCode, persistResult(reg, hpiResult.capCode, 1)) 
	  case _ =>  response = getCapCode(reg) 
	}
	Full(PlainTextResponse( response ))
    }
    
    def getCapCode(reg: String): String = {
      val capCode = ComponentRegistry.capCodeService.getCapCodeByReg(reg) getOrElse "Missing"
      persistResult(reg, capCode, 0)      
      capCode
    }
    
    def persistResult(reg : String, capCode: String, matchingMethod : Int) = {
	val hpiQuery = new HPIQuery
	hpiQuery.queryName = "Test query"
	Model.persist(hpiQuery)

	val hpiResult = new HPIResult
	hpiResult.reg = reg      
	hpiResult.capCode = capCode 
	hpiResult.bcaDescription = "description"
	hpiResult.matchingMehtod = matchingMethod
	hpiResult.hpiQuery = hpiQuery
	Model.persist(hpiResult)
    }
    
    
    
}

