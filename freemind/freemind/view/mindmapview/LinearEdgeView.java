/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2001  Joerg Mueller <joergmueller@bigfoot.com>
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
 */
/*$Id: LinearEdgeView.java,v 1.9.30.1.6.1 2007-04-09 11:36:13 dpolivaev Exp $*/

package freemind.view.mindmapview;

import java.awt.Graphics2D;
import java.awt.Color;

/**
 * This class represents a single Edge of a MindMap.
 */
public class LinearEdgeView extends EdgeView {

    public LinearEdgeView() {
	super();
    }

    public void paint(Graphics2D g) {
	g.setColor(getColor());
	g.setStroke(getStroke());
        setRendering(g);
	int w=getWidth();
	if (w<=1) {
		g.drawLine(start.x,start.y,end.x,end.y);
		if(isTargetEclipsed(g)){
			g.drawLine(start.x,start.y,end.x,end.y);
		}
	}
	else {
		// a little horizontal part because of line cap
		int dx=w/3+1;
		if(getTarget().isLeft()) dx=-dx;
		int xs[] = { start.x, start.x+dx, end.x-dx, end.x };
		int ys[] = { start.y, start.y, end.y, end.y};
		g.drawPolyline(xs,ys,4);
		if(isTargetEclipsed(g)){
			g.drawPolyline(xs,ys,4);
		}
	}
	
	super.paint(g);
    }
    
    public Color getColor() {
	return getModel().getColor();
    }
}
