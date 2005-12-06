﻿/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2005  Joerg Mueller, Daniel Polansky, Christian Foltin and others.
 *
 *See COPYING for Details
 *
 *This program is free software; you can redistribute it and/or
 *modify it under the terms of the GNU General Public License
 *as published by the Free Software Foundation; either version 2
 *of the License, or (at your option) any later version.
 *
 *This program is distributed in the hope that it will be useful,
 *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *GNU General Public License for more details.
 *
 *You should have received a copy of the GNU General Public License
 *along with this program; if not, write to the Free Software
 *Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Created on 25.04.2005
 */
import visorFreeMind.*;
/**
* Main Class
* Author : Juan Pedro de Andres
* This class is configured to be the called with MSTAC (Motion-Twin ActionScript 2 Compiler)
*/
class visorFreeMind.Main {
		
		static var browser;

		static function redefineRightMenu(){
			var mycm=new ContextMenu();
			mycm.hideBuiltInItems();
			mycm.onSelect = visorFreeMind.Main.copyInfoNodeOver;
			var copy=new ContextMenuItem("move back",visorFreeMind.Main.backward);
			mycm.customItems.push(copy);
			copy=new ContextMenuItem("move forward",visorFreeMind.Main.forward);
			mycm.customItems.push(copy);
			copy=new ContextMenuItem("copy to clipboard",visorFreeMind.Main.getNodeText);
			mycm.customItems.push(copy);
			_root.menu = mycm;
		}
		
		static function backward(){
			if(browser.posXmls>0){
				browser.posXmls--;
				browser.fileName=browser.visitedMM[browser.posXmls];
				browser.genMindMap(3);
			}
		}
		static function forward(){
			if(browser.posXmls<(browser.visitedMM.length-1)){
			browser.posXmls++;
			browser.fileName=browser.visitedMM[browser.posXmls];
			browser.genMindMap(3);
			}
		}		
		static function copyInfoNodeOver(){
			Node.saveTxt();
			if(Node.lastOverTxt=="")
				_root.menu.customItems[2].enabled = false;
			else
				_root.menu.customItems[2].enabled = true;
		}
		
		static 	function getNodeText(){
				System.useCodepage = true;
				System.setClipboard(Node.lastOverTxt);
				System.useCodepage = false;
		}
		
		
		static var initied=false;
		public static function main():Void{
						_root.onEnterFrame = function()
				{
					Main.run();
				};
		}
		
		static public function run ():Boolean
	   {
	   	   if(initied==true)return true;
	   	   else initied=true;
	   	   Flashout.init();
		   trace("Starting flash FreeMind Browser",2);

			// set the Flash movie to have a fixed anchor
		    // in the top left corner of the screen.
			Stage.align = "LT";

			// prevent the Flash movie from resizing when the browser window
		    // changes size.
			Stage.scaleMode = "noScale";

		    // tell the Macromedia Flash Player 6 to use the traditional code page
		    // of the operating system running the player
			System.useCodepage = false;

		   // If not defined init mindmap file, use default (index.mm)
		   if(_root.openUrl!=null)
		   		Node.openUrl=_root.openUrl;
		   if(_root.noElipseMode!=null)
		   		Edge.elipseMode=false;
		   if(!isNaN(_root.defaultWordWrap))
		   		Node.defaultWordWrap=_root.defaultWordWrap;
		   if(_root.startCollapsedToLevel!=null)
		   		Browser.startCollapsedToLevel=_root.startCollapsedToLevel;
		   if(_root.mainNodeShape=="rectangle")
		   		Node.mainNodeShape="rectangle";
		   if(_root.initLoadFile!=null){
				trace("initial mindmap: "+_root.initLoadFile,2);
				browser=new Browser(_root.initLoadFile,_root);
		   }
			else{
				trace("initial mindmap: index.mm",2);
				browser=new Browser("index.mm",_root);
			}

		    redefineRightMenu();

			return true;
		}


}
