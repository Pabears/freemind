/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2000-2007  Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitri Polivaev and others.
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
/* $Id: RootMainView.java,v 1.1.2.2 2007-04-09 12:01:19 dpolivaev Exp $ */
package freemind.view.mindmapview;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;

import freemind.controller.Controller;
import freemind.main.FreeMind;
import freemind.main.Resources;

class RootMainView extends MainView{

    /* (non-Javadoc)
     * @see freemind.view.mindmapview.NodeView.MainView#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        Dimension prefSize = super.getPreferredSize();
        prefSize.width *= 1.1;
        prefSize.height *= 2;
        return prefSize;
    }
    
    public void paint(Graphics graphics) {
        Graphics2D g = (Graphics2D)graphics;
        
        if (getNodeView().getModel()==null) return;

            paintSelected(g);
            paintDragOver(g);

        //Draw a root node
        g.setColor(Color.gray);
        g.setStroke(new BasicStroke(1.0f));
            setRendering(g);
        g.drawOval(1, 1, getWidth()-2, getHeight()-2);
            if (!getNodeView().getMap().getController().getAntialiasAll()) {
               g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF); }

        super.paint(g);
        }

       public void paintDragOver(Graphics2D graphics) {
            final int draggedOver = getDraggedOver();
            if (draggedOver == NodeView.DRAGGED_OVER_SON) {
                  graphics.setPaint( 
                          new GradientPaint(
                                  getWidth()/4,
                                  0,
                                  getNodeView().getMap().getBackground(), 
                                  getWidth()*3/4, 
                                  0, 
                                  NodeView.dragColor)
                                  );
                  graphics.fillRect(
                          getWidth()/4, 
                          0, 
                          getWidth()-1, 
                          getHeight()-1); 
            } else if (draggedOver == NodeView.DRAGGED_OVER_SON_LEFT) {
                  graphics.setPaint( 
                          new GradientPaint(
                                  getWidth()*3/4,
                                  0,
                                  getNodeView().getMap().getBackground(), 
                                  getWidth()/4, 
                                  0, 
                                  NodeView.dragColor)
                                  );
                  graphics.fillRect(0, 
                          0, 
                          getWidth()*3/4, 
                          getHeight()-1);
            }
        }


       protected void setRendering(Graphics2D g) {
          final Controller controller = getNodeView().getMap().getController();
        if (controller.getAntialiasEdges() || controller.getAntialiasAll()) {
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); }}


        protected void paintBackground(
            Graphics2D graphics,
            Color color) {
            graphics.setColor(color);
            graphics.fillOval(1,
                    1,
                    getWidth()-1,
                    getHeight()-1);
        }

        Point getCenterPoint() {
            Point in= new Point(getWidth() / 2, getHeight() / 2);
            return in;
        }

        /* fc, 26.06.2005 */
        /** Returns the point the edge should start given the index of the child node 
         * that should be connected.
         */
        Point getOutPoint(Point destinationPoint, boolean isLeft) {
            Point p = new Point(destinationPoint);
            convertPointFromMap(p);
            double nWidth = getWidth();
            double nHeight = getHeight();
            Point centerPoint = new Point((int) (nWidth / 2f), (int) (nHeight / 2f));
            // assume, that destinationPoint is on the right:
            double angle = Math.atan((p.y - centerPoint.y + 0f)
                    / (p.x - centerPoint.x + 0f));
            if (p.x < centerPoint.x) {
                angle += Math.PI;
            }
            // now determine point on ellipsis corresponding to that angle:
            Point out = new Point(centerPoint.x
                    + (int) (Math.cos(angle) * nWidth / 2f), centerPoint.y
                    + (int) (Math.sin(angle) * (nHeight) / 2f));
            convertPointToMap(out);
            return out;
        }
        /* end fc, 26.06.2005 */

        /**
         * Returns the Point where the InEdge
         * should arrive the Node.
         */
        Point getInPoint() {
            Point in =  new Point(0, getHeight() / 2);
            convertPointToMap(in);
            return in;
        }

        public void setDraggedOver(Point p) {
            setDraggedOver ((dropPosition(p.getX())) ? NodeView.DRAGGED_OVER_SON_LEFT : NodeView.DRAGGED_OVER_SON); 
        }

        /* (non-Javadoc)
         * @see freemind.view.mindmapview.NodeView#getStyle()
         */
        String getStyle() {
            return Resources.getInstance().getProperty(FreeMind.RESOURCES_ROOT_NODE_STYLE);
        }

        /**
         * Returns the relative position of the Edge
         */
        int getAlignment() {
            return NodeView.ALIGN_CENTER;
        }

        
        public int getTextWidth() {
            return super.getTextWidth() - getWidth()/10;
        }
        /* (non-Javadoc)
         * @see freemind.view.mindmapview.NodeView#getTextX()
         */
        public int getTextX() {
            return getIconWidth() + getWidth()/20;
        }

        public boolean dropAsSibling (double xCoord) {
            return false;
        }

        /** @return true if should be on the left, false otherwise.*/
        public boolean dropPosition (double xCoord) {
           return xCoord < getSize().width*1/2 ; 
        }

}