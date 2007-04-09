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
/* $Id: MapView.java,v 1.30.16.16.2.10.2.2 2007-04-09 12:01:18 dpolivaev Exp $ */
package freemind.view.mindmapview;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

import freemind.controller.Controller;
import freemind.controller.NodeKeyListener;
import freemind.controller.NodeMotionListener;
import freemind.controller.NodeMouseMotionListener;
import freemind.main.Tools;
import freemind.modes.MindMap;
import freemind.modes.MindMapArrowLink;
import freemind.modes.MindMapLink;
import freemind.modes.MindMapNode;


/**
 * This class represents the view of a whole MindMap
 * (in analogy to class JTree).
 */
public class MapView extends JPanel implements Printable, Autoscroll{
	private class Selected {
		private Vector mySelected = new Vector();
		public Selected() {};
		public void clear() {
			if(size() >0 ) {
				removeSelectionForHooks(get(0));
			}
			mySelected.clear();
			logger.finest("Cleared selected.");
		}
		public int size() { return mySelected.size();
		}
		public void remove(NodeView node) {
			if(mySelected.indexOf(node)==0) {
				removeSelectionForHooks(node);
			}
			mySelected.remove(node);
			logger.finest("Removed selected "+node);
		}
		public void add(NodeView node) {
			if(size() >0 ) {
				removeSelectionForHooks(get(0));
			}
			mySelected.add(0, node);
			addSelectionForHooks(node);
			logger.finest("Added selected "+node + "\nAll="+mySelected);
		}
		private void removeSelectionForHooks(NodeView node) {
		    if(node.getModel() == null)
		        return;
		    getModel().getModeController().onLooseFocusHook(node.getModel());
		}
		private void addSelectionForHooks(NodeView node) {
            getModel().getModeController().onReceiveFocusHook(node.getModel());
		}
		public NodeView get(int i) { return (NodeView) mySelected.get(i);
		}
		public boolean contains(NodeView node) { return mySelected.contains(node);
		}
		/**
		 */
		public void moveToFirst(NodeView newSelected) {
			if(contains(newSelected)) {
				int pos = mySelected.indexOf(newSelected);
				if( pos > 0 ){ // move
				if(size() >0 ) {
					removeSelectionForHooks(get(0));
				}
					mySelected.remove(newSelected);
					mySelected.add(0, newSelected);
				}
			} else {
				add(newSelected);
			}
			addSelectionForHooks(newSelected);
			logger.finest("MovedToFront selected "+newSelected + "\nAll="+mySelected);
		}
	}

	//	Logging:
	private static java.util.logging.Logger logger;

	private class DelayedScroller extends ComponentAdapter{
		/**
		 * the only one interface method.
		 */
		public void componentMoved(ComponentEvent e){
			if (m_map != null) { // fc, 22.2.2005: bug fix.
                m_map.scrollNodeToVisible(m_node, m_extraWidth);
            }
			m_map = null;
			m_node = null;
		}

		public void scrollNodeToVisible(MapView map, NodeView node, int extraWidth){
			deactivate();
			m_map = map;
			m_node = node;
			m_extraWidth = extraWidth;
			node.addComponentListener(this);
		}

		public void deactivate(){
			if (m_node != null)
				m_node.removeComponentListener(this);
		}
		MapView m_map = null;
		NodeView m_node = null;
		int m_extraWidth = 0;
	}

	private DelayedScroller m_delayedScroller = new DelayedScroller();

    private MindMap model;
    private NodeView rootView = null;
    private Selected selected = new Selected();
    private Controller controller;
    private float zoom=1F;
    private boolean disableMoveCursor = true;
    private int siblingMaxLevel;
    private boolean isPrinting = false; // use for remove selection from print
    private NodeView shiftSelectionOrigin = null;
    private int maxNodeWidth = 0;
    private Color background = null;
	private Rectangle boundingRectangle = null;
	private boolean fitToPage = true;
	private boolean isPreparedForPrinting = false;

    /** Used to identify a right click onto a link curve.*/
    private Vector/* of ArrowLinkViews*/ ArrowLinkViews;

    private Point rootContentLocation;
    //
    // Constructors
    //

    public MapView( MindMap model, Controller controller ) {
        super();

        this.model = model;
        this.controller = controller;
		if(logger == null)
			logger = controller.getFrame().getLogger(this.getClass().getName());

        this.setAutoscrolls(true);

        this.setLayout( new MindMapLayout( ) );

        initRoot();

        setBackground(getModel().getBackgroundColor());

        addMouseListener( controller.getMapMouseMotionListener() );
        addMouseMotionListener( controller.getMapMouseMotionListener() );
        addMouseWheelListener( controller.getMapMouseWheelListener() );

		// fc, 20.6.2004: to enable tab for insert.
		setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
		setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, Collections.EMPTY_SET);
		setFocusTraversalKeys(KeyboardFocusManager.UP_CYCLE_TRAVERSAL_KEYS, Collections.EMPTY_SET);
		// end change.

        // like in excel - write a letter means edit (PN)
        // on the other hand it doesn't allow key navigation (sdfe)
        disableMoveCursor = Tools.safeEquals(
                                             controller.getProperty("disable_cursor_move_paper"),"true");
    }

    public void initRoot() {
        rootContentLocation = new Point();
        rootView = NodeViewFactory.getInstance().newNodeView( getModel().getRootNode(), 0, this, this );
        rootView.insert();
        revalidate();
    }

        public int getMaxNodeWidth() {
        if (maxNodeWidth == 0) {
            try {
                maxNodeWidth = Integer.parseInt(controller.getProperty("max_node_width")); }
            catch (NumberFormatException e) {
                maxNodeWidth = Integer.parseInt(controller.getProperty("el__max_default_window_width")); }}
        return maxNodeWidth; }

    //
    // Navigation
    //

    /**
     * Problem: Before  scrollRectToVisible is called, the node has the location (0,0), ie. the location first gets
     * calculated after the scrollpane is actually scrolled. Thus, as a workaround, I simply call scrollRectToVisible
     * twice, the first time the location of the node is calculated, the second time the scrollPane is actually
     * scrolled.
     */
    public void centerNode( final NodeView node ) {
        JViewport viewPort = (JViewport)getParent();
        if(! (isValid() &&viewPort.isValid())){
            EventQueue.invokeLater(new Runnable(){
                public void run() {
                    centerNode(node) ;
                }
            });
            return;
        }
        Dimension d = viewPort.getExtentSize();
        JComponent content = node.getContent();
		Rectangle rect = new Rectangle(content.getWidth()/2 - d.width/2,
                                       content.getHeight()/2 - d.height/2,
			d.width, d.height);

		// One call of scrollRectToVisible suffices
		// after patching the FreeMind.java
		// and the FreeMindApplet
        content.scrollRectToVisible(rect);
    }

    // scroll with extension (PN 6.2)
    public void scrollNodeToVisible( NodeView node ) {
			scrollNodeToVisible( node, 0);
    }

    // scroll with extension (PN)
    // e.g. the input field is bigger than the node view => scroll in order to
    //      fit the input field into the screen
    public void scrollNodeToVisible( NodeView node, int extraWidth) {
        //see centerNode()
		if (! node.isValid()){
			m_delayedScroller.scrollNodeToVisible(this, node, extraWidth);
			return;
		}
		m_delayedScroller.deactivate();
        final int HORIZ_SPACE  = 10;
        final int HORIZ_SPACE2 = 20;
        final int VERT_SPACE   = 5;
        final int VERT_SPACE2  = 10;

        // get left/right dimension
        final JComponent nodeContent = node.getContent();
        int width = nodeContent.getWidth();
        if (extraWidth < 0) { // extra left width
			width -= extraWidth;
        }
        else {                // extra right width
            width += extraWidth;
        }
        if (node != null) {
            nodeContent.scrollRectToVisible( new Rectangle(- HORIZ_SPACE, - VERT_SPACE,
                    width + HORIZ_SPACE2, nodeContent.getHeight() + VERT_SPACE2) );
        }
    }
	/**
	 * Returns the size of the visible part of the view in view coordinates.
	 */
	public Dimension getViewportSize(){
		JViewport mapViewport = (JViewport)getParent();
		return mapViewport == null ? null : mapViewport.getSize();
	}

    /**
     * Scroll the viewport of the map to the south-west, i.e. scroll the map itself to the north-east.
     */
    public void scrollBy(int x, int y) {
        JViewport mapViewport = (JViewport)getParent();
		if(mapViewport != null){
        Point currentPoint = mapViewport.getViewPosition();                 // Get View Position
        currentPoint.translate(x, y);                                       // Add the difference to it
        // Watch for the boundaries
        //    Low boundaries
        if (currentPoint.getX()<0) {
            currentPoint.setLocation(0, currentPoint.getY()); }
        if (currentPoint.getY()<0) {
            currentPoint.setLocation(currentPoint.getX(), 0); }
        //    High boundaries
        double maxX = getSize().getWidth() - mapViewport.getExtentSize().getWidth();     // getView() gets viewed area - JPanel
        double maxY = getSize().getHeight() - mapViewport.getExtentSize().getHeight();
        if (currentPoint.getX()>maxX) {
            currentPoint.setLocation(maxX, currentPoint.getY()); }
        if (currentPoint.getY()>maxY) {
            currentPoint.setLocation(currentPoint.getX(), maxY); }
		mapViewport.setViewPosition(currentPoint);
    }
    }

    //
    // Node Navigation
    //

    private NodeView getNeighbour(int directionCode) {
        NodeView oldSelected = getSelected();
        NodeView newSelected = null;

        switch (directionCode) {
        case KeyEvent.VK_LEFT:
            setSiblingMaxLevel(oldSelected.getModel().getNodeLevel()); // for case of return
            if(oldSelected.getModel().isRoot()){
                LinkedList left = (oldSelected).getLeft(true);
                if (left.size() == 0) return null;
                newSelected = oldSelected.getPreferredChild();
                if (!left.contains(newSelected)) {
                    newSelected = (NodeView)left.getFirst();
                }
            } else if(!oldSelected.isLeft()) {
                newSelected = oldSelected.getParentView();
            } else {
                if (oldSelected.getModel().isFolded()) { // If folded in the direction, unfold
                    model.getModeController().setFolded(oldSelected.getModel(), false);
                    return null;
                }

                if (oldSelected.getChildrenViews().size() == 0) return null;
                newSelected = oldSelected.getPreferredChild();
            }
            setSiblingMaxLevel(newSelected.getModel().getNodeLevel());
            break;

        case KeyEvent.VK_RIGHT:
            setSiblingMaxLevel(oldSelected.getModel().getNodeLevel()); // for case of return
            if(oldSelected.isRoot()) {
                LinkedList right = (oldSelected).getRight(true);
                if (right.size() == 0) return null;
                newSelected = oldSelected.getPreferredChild();
                if (!right.contains(newSelected)) {
                    newSelected = (NodeView)right.getFirst();
                }
            } else if(oldSelected.isLeft()) {
                newSelected = oldSelected.getParentView();
            } else {
                if (oldSelected.getModel().isFolded()) { // If folded in the direction, unfold
//                  URGENT: Change to controller setFolded.
//                    getModel().setFolded(oldSelected.getModel(), false);
                    getController().getModeController().setFolded(oldSelected.getModel(), false);
                    return null;
                }

                if (oldSelected.getChildrenViews().size() == 0) return null;
                newSelected = oldSelected.getPreferredChild();
            }
            setSiblingMaxLevel(newSelected.getModel().getNodeLevel());
            break;

        case KeyEvent.VK_UP:
            newSelected = oldSelected.getPreviousSibling();
            break;

        case KeyEvent.VK_DOWN:
            newSelected = oldSelected.getNextSibling();
            break;

        case KeyEvent.VK_PAGE_UP:
            newSelected = oldSelected.getPreviousPage();
            break;

        case KeyEvent.VK_PAGE_DOWN:
            newSelected = oldSelected.getNextPage();
            break;
        }
        return newSelected;
    }

    public void move(KeyEvent e) {
        NodeView newSelected = getNeighbour(e.getKeyCode());
        if (newSelected != null) {
            extendSelectionWithKeyMove(newSelected, e);
            scrollNodeToVisible( newSelected );
            e.consume();
        }
    }

    private void extendSelectionWithKeyMove(NodeView newlySelectedNodeView, KeyEvent e) {
        if (e.isShiftDown()) {
            // left or right
            if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                shiftSelectionOrigin = null;
                NodeView toBeNewSelected = newlySelectedNodeView.isParentOf(getSelected()) ?
                    newlySelectedNodeView : getSelected();

                selectBranch(toBeNewSelected, false);
                makeTheSelected(toBeNewSelected);
                return; }

            if (shiftSelectionOrigin == null) {
                shiftSelectionOrigin = getSelected(); }

            int deltaY = newlySelectedNodeView.getY() - shiftSelectionOrigin.getY();

            // page up and page down
            NodeView currentSelected = getSelected();
            if (e.getKeyCode() == KeyEvent.VK_PAGE_UP) {
                for (;;) {
                    if (currentSelected.getY() > shiftSelectionOrigin.getY())
                        deselect(currentSelected);
                    else
                        makeTheSelected(currentSelected);
                    if (currentSelected.getY() <= newlySelectedNodeView.getY())
                        break;
                    currentSelected = currentSelected.getPreviousSibling(); }
                return; }

            if (e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                for (;;) {
                    if (currentSelected.getY() < shiftSelectionOrigin.getY())
                        deselect(currentSelected);
                    else
                        makeTheSelected(currentSelected);
                    if (currentSelected.getY() >= newlySelectedNodeView.getY())
                        break;
                    currentSelected = currentSelected.getNextSibling(); }
                return; }

            boolean enlargingMove = (deltaY > 0) && (e.getKeyCode() == KeyEvent.VK_DOWN) ||
                (deltaY < 0) && (e.getKeyCode() == KeyEvent.VK_UP);

            if (enlargingMove) {
                toggleSelected(newlySelectedNodeView); }
            else {
                toggleSelected(getSelected());
                makeTheSelected(newlySelectedNodeView); }}
        else {
            shiftSelectionOrigin = null;
            selectAsTheOnlyOneSelected(newlySelectedNodeView); }}

    public void moveToRoot() {
        selectAsTheOnlyOneSelected( getRoot() );
        centerNode( getRoot() );
    }


    /**
     * Select the node, resulting in only that one being selected.
     */
    public void selectAsTheOnlyOneSelected(NodeView newSelected) {
        selectAsTheOnlyOneSelected(newSelected, true);
    }
    public void selectAsTheOnlyOneSelected(NodeView newSelected, boolean requestFocus) {
        LinkedList oldSelecteds = getSelecteds();
        //select new node
        this.selected.clear();
        this.selected.add(newSelected);
        if(requestFocus)
            newSelected.requestFocus();
        
//        getController().getMode().getDefaultModeController().onReceiveFocusHook(newSelected.getModel());
        // set last focused as preferred (PN)
        if (newSelected.getModel().getParentNode() != null) {
            ((NodeView)newSelected.getParent()).setPreferredChild(newSelected);
        }

        scrollNodeToVisible(newSelected);
        newSelected.repaint();

        for(ListIterator e = oldSelecteds.listIterator();e.hasNext();) {
            NodeView oldSelected = (NodeView)e.next();
            if (oldSelected != null) {
                oldSelected.repaint();
            }
        }
    }

    /**
     * Add the node to the selection if it is not yet there, remove it otherwise.
     */
    public void toggleSelected(NodeView newSelected) {
        NodeView oldSelected = getSelected();
        if (isSelected(newSelected)) {
            if (selected.size() > 1) {
                selected.remove(newSelected);
                oldSelected=newSelected;
            }
        }
        else {
            selected.add(newSelected);
        }
        getSelected().requestFocus();
        getSelected().repaint();
        if(oldSelected != null)
            oldSelected.repaint();
    }

    /**
     * Add the node to the selection if it is not yet there, making it the focused selected node.
     */

    public void makeTheSelected(NodeView newSelected) {
        if (isSelected(newSelected)) {
            selected.moveToFirst(newSelected);
        } else {
			selected.add(newSelected);
        }
        getSelected().requestFocus();
        getSelected().repaint(); }

    public void deselect(NodeView newSelected) {
        if (isSelected(newSelected)) {
            selected.remove(newSelected);
            newSelected.repaint();
        }
    }


    /**
     * Select the node and his descendants. On extend = false clear up the previous selection.
     * if extend is false, the past selection will be empty.
     * if yes, the selection will extended with this node and its children
     */
    public void selectBranch(NodeView newlySelectedNodeView, boolean extend) {
        //if (!extend || !isSelected(newlySelectedNodeView))
        //    toggleSelected(newlySelectedNodeView);
        if (!extend) {
            selectAsTheOnlyOneSelected(newlySelectedNodeView); }
        else if (!isSelected(newlySelectedNodeView)) {
            toggleSelected(newlySelectedNodeView); }
        //select(newSelected,extend);
        for (ListIterator e = newlySelectedNodeView.getChildrenViews().listIterator(); e.hasNext(); ) {
            NodeView target = (NodeView)e.next();
            selectBranch(target,true);
        }
    }

    public boolean selectContinuous(NodeView newSelected) {
        /* fc, 25.1.2004: corrected due to completely inconsistent behaviour.*/
        NodeView oldSelected = null;
        // search for the last already selected item among the siblings:
        LinkedList selList = getSelecteds();
        ListIterator j = selList.listIterator(/*selList.size()*/);
        while(j.hasNext()) {
            NodeView selectedNode = (NodeView)j.next();
            if(selectedNode != newSelected && newSelected.isSiblingOf(selectedNode)) {
                oldSelected = selectedNode;
                break;
            }
        }
        // no such sibling found. select the new one, and good bye.
        if(oldSelected == null) {
            if(!isSelected(newSelected)) {
                toggleSelected(newSelected);
                return true;
            }
            return false;
        }
        // fc, bug fix: only select the nodes on the same side:
        boolean oldPositionLeft = oldSelected.isLeft();
        boolean newPositionLeft = newSelected.isLeft();
        /* find old starting point. */
        ListIterator i = newSelected.getSiblingViews().listIterator();
        while (i.hasNext()) {
            NodeView nodeView = (NodeView)i.next();
            if ( nodeView == oldSelected ) {
                break;
            }
        }
        /* Remove all selections for the siblings in the connected component between old and new.*/
        ListIterator i_backup = i;
        while (i.hasNext()) {
            NodeView nodeView = (NodeView)i.next();
            if((nodeView.isLeft() == oldPositionLeft || nodeView.isLeft() == newPositionLeft)) {
                if ( isSelected(nodeView) )
                    deselect(nodeView);
                else
                    break;
            }
        }
        /* other direction. */
        i = i_backup;
        if(i.hasPrevious()) {
            i.previous(); /* this is old selected! */
            while (i.hasPrevious()) {
                NodeView nodeView = (NodeView)i.previous();
                if(nodeView.isLeft() == oldPositionLeft || nodeView.isLeft() == newPositionLeft){
                    if ( isSelected(nodeView) )
                        deselect(nodeView);
                    else
                        break;
                }
            }
        }
        /* reset iterator */
        i = newSelected.getSiblingViews().listIterator();
        /* find starting point. */
        i = newSelected.getSiblingViews().listIterator();
        while (i.hasNext()) {
            NodeView nodeView = (NodeView)i.next();
            if ( nodeView == newSelected || nodeView == oldSelected ) {
                if( ! isSelected(nodeView) )
                    toggleSelected(nodeView);
                break;
            }
        }
        /* select all up to the end point.*/
        while (i.hasNext()) {
            NodeView nodeView = (NodeView)i.next();
            if( (nodeView.isLeft() == oldPositionLeft || nodeView.isLeft() == newPositionLeft) && ! isSelected(nodeView) )
                toggleSelected(nodeView);
            if ( nodeView == newSelected || nodeView == oldSelected ) {
                break;
            }
        }
        // now, make oldSelected the last of the list in order to make this repeatable:
        toggleSelected(oldSelected);
        toggleSelected(oldSelected);
        return true;
    }

    //
    // get/set methods
    //

    public MindMap getModel() {
        return model;
    }

    // e.g. for dragging cursor (PN)
    public void setMoveCursor(boolean isHand) {
        int requiredCursor = (isHand && !disableMoveCursor)
            ? Cursor.MOVE_CURSOR : Cursor.DEFAULT_CURSOR;
        if (getCursor().getType() != requiredCursor) {
            setCursor(new Cursor(requiredCursor));
        }
    }

    NodeMouseMotionListener getNodeMouseMotionListener() {
        return getController().getNodeMouseMotionListener();
    }

    NodeMotionListener getNodeMotionListener() {
        return getController().getNodeMotionListener();
    }

    NodeKeyListener getNodeKeyListener() {
        return getController().getNodeKeyListener();
    }

    DragGestureListener getNodeDragListener() {
        return getController().getNodeDragListener();
    }

    DropTargetListener getNodeDropListener() {
        return getController().getNodeDropListener();
    }

    public NodeView getSelected() {
        if(selected.size() > 0)
            return selected.get(0);
        else
            return null;
    }

    private NodeView getSelected(int i) {
        return selected.get(i);
    }

    public LinkedList getSelecteds() {
        // return an ArrayList of NodeViews.
        LinkedList result = new LinkedList();
        for (int i=0; i<selected.size();i++) {
            result.add( getSelected(i) );
        }
        return result;
    }


     /**
     * @return an ArrayList of MindMapNode objects.
     */
    public ArrayList /*of MindMapNodes*/ getSelectedNodesSortedByY() {
        TreeMap sortedNodes = new TreeMap();
        for (int i=0; i<selected.size();i++) {
            sortedNodes.put(new Integer(getSelected(i).getY()), getSelected(i).getModel()); }

        ArrayList selectedNodes = new ArrayList();
        for(Iterator it = sortedNodes.entrySet().iterator();it.hasNext();) {
            selectedNodes.add( ((Map.Entry)it.next()).getValue() ); }

        return selectedNodes;
    }

    public boolean isSelected(NodeView n) {
        if (isPrinting) return false;
        return selected.contains(n);
    }


    public float getZoom() {
        return zoom; }

    public int getZoomed(int number) {
        return (int)(number*zoom); }

    public void setZoom(float zoom) {
        this.zoom = zoom;
        getRoot().updateAll();
        revalidate();
        Runnable callScroll = new Runnable() {
            public void run() {
                scrollNodeToVisible(getSelected());
            }
        };
        SwingUtilities.invokeLater(callScroll);
    }

        /*****************************************************************
         **             P A I N T I N G                                 **
         *****************************************************************/
    
    //    private static Image image = null;
    
        /* (non-Javadoc)
         * @see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        public void paint(Graphics g) {
            getRoot().getContent().getLocation(rootContentLocation);
            SwingUtilities.convertPointToScreen(rootContentLocation, getRoot());
            super.paint(g);
        }

    public void paintChildren(Graphics graphics) {
        // first tries for background images.
//        if(image ==  null) {
//            image = MindIcon.factory("ksmiletris").getIcon(controller.getFrame()).getImage();
//        }
//        graphics.drawImage(image, 0, 0, getHeight(), getWidth(), null);
        HashMap labels = new HashMap();
        ArrowLinkViews = new Vector();
        collectLabels(rootView, labels);
        super.paintChildren(graphics);
        paintLinks(rootView, (Graphics2D)graphics, labels, null);
    }


    /** collect all existing labels in the current map.*/
    protected void collectLabels(NodeView source, HashMap labels) {
        // check for existing registry:
        if(getModel().getLinkRegistry()==null)
            return;
        // apply own label:
        String label = getModel().getLinkRegistry().getLabel(source.getModel());
        if(label != null)
            labels.put(label, source);
        for(ListIterator e = source.getChildrenViews().listIterator(); e.hasNext(); ) {
            NodeView target = (NodeView)e.next();
            collectLabels(target, labels);
        }
    }

    protected void paintLinks(NodeView source, Graphics2D graphics, HashMap labels, HashSet /* MindMapLink s*/ LinkAlreadyVisited) {
        // check for existing registry:
        if(getModel().getLinkRegistry()==null)
            return;
        if(LinkAlreadyVisited == null)
            LinkAlreadyVisited = new HashSet();
        // references first
        // paint own labels:
        Vector vec = getModel().getLinkRegistry().getAllLinks(source.getModel());
        for(int i = 0; i< vec.size(); ++i) {
            MindMapLink ref = (MindMapLink) vec.get(i);
            if(LinkAlreadyVisited.add(ref)) {
                // determine type of link
                if(ref  instanceof MindMapArrowLink) {
                    ArrowLinkView arrowLink = new ArrowLinkView((MindMapArrowLink) ref, getNodeView(ref.getSource()), getNodeView(ref.getTarget()));
                    arrowLink.paint(graphics);
                    ArrowLinkViews.add(arrowLink);
                    // resize map?
                    // adjust container size
                    Rectangle rec = arrowLink.getBounds();
                    // the following does not work correctly. fc, 23.10.2003:
//                     if (rec.x < 0) {
//                         getMindMapLayout().resizeMap(rec.x);
//                     } else if (rec.x+rec.width > getSize().width) {
//                         getMindMapLayout().resizeMap(rec.x+rec.width);
//                     }

                }
            }
        }
        for(ListIterator e = source.getChildrenViews().listIterator(); e.hasNext(); ) {
            NodeView target = (NodeView)e.next();
            paintLinks(target, graphics, labels, LinkAlreadyVisited);
        }
    }

    public MindMapArrowLink detectCollision(Point p) {
        if(ArrowLinkViews == null)
            return null;
        for(int i = 0; i < ArrowLinkViews.size(); ++i) {
            ArrowLinkView arrowView = (ArrowLinkView) ArrowLinkViews.get(i);
            if(arrowView.detectCollision(p))
                return arrowView.getModel();
        }
        return null;
    }

	/**
	  * Call preparePrinting() before printing
	  * and  endPrinting() after printing
	  * to minimize calculation efforts
	  */
	public void preparePrinting() {
		_preparePrinting();
		isPreparedForPrinting = true;
	}

	private void _preparePrinting() {
		if (isPreparedForPrinting == false){
			isPrinting = true;
			/* repaint for printing:*/
			setZoom(getZoom());
			background = getBackground();
			boundingRectangle = getInnerBounds();
            fitToPage = Tools.safeEquals(controller.getProperty("fit_to_page"),"true");
		}
	}

	 /**
	  * Call preparePrinting() before printing
	  * and  endPrinting() after printing
	  * to minimize calculation efforts
	  */
	public void endPrinting() {
		isPreparedForPrinting = false;
		_endPrinting();
	}

	 private void _endPrinting() {
		if (isPreparedForPrinting == false){
			setBackground(background);
			isPrinting = false;
			/* repaint for end printing:*/
			setZoom(getZoom());
		}
	 }

    public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {
        // TODO:
        // ask user for :
        // - center in page (in page format ?)
        // - print zoom or maximize (in page format ?)
        // - print selection only
        // remember those parameters from one session to another
        //    (as orientation & margin from pf)

        // User parameters

        double userZoomFactor = 1;
        try {
            userZoomFactor = Double.parseDouble(controller.getProperty("user_zoom")); }
        catch (Exception e) {}
        userZoomFactor = Math.max(0,userZoomFactor);
        userZoomFactor = Math.min(2,userZoomFactor);

        // TODO: read user parameters from properties, make sure the multiple page
        // printing really works, have look at Book class.

        if (fitToPage && pageIndex > 0) {
            return Printable.NO_SUCH_PAGE; }

        Graphics2D graphics2D = (Graphics2D)graphics;

		_preparePrinting();
        double zoomFactor = 1;
        if (fitToPage) {
            double zoomFactorX = pageFormat.getImageableWidth()/boundingRectangle.getWidth();
            double zoomFactorY = pageFormat.getImageableHeight()/boundingRectangle.getHeight();
            zoomFactor = Math.min (zoomFactorX, zoomFactorY); }
        else {
            zoomFactor = userZoomFactor;

            int nrPagesInWidth = (int)Math.ceil(zoomFactor * boundingRectangle.getWidth() /
                                                pageFormat.getImageableWidth());
            int nrPagesInHeight = (int)Math.ceil(zoomFactor *boundingRectangle.getHeight() /
                                                 pageFormat.getImageableHeight());
            if (pageIndex >= nrPagesInWidth * nrPagesInHeight) {
                return Printable.NO_SUCH_PAGE; }
            int yPageCoord = (int)Math.floor(pageIndex / nrPagesInWidth);
            int xPageCoord = pageIndex - yPageCoord * nrPagesInWidth;

            graphics2D.translate(- pageFormat.getImageableWidth() * xPageCoord,
                                 - pageFormat.getImageableHeight() * yPageCoord); }

        graphics2D.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
        graphics2D.scale(zoomFactor, zoomFactor);
        graphics2D.translate(-boundingRectangle.getX(), -boundingRectangle.getY());


        print(graphics2D);
		_endPrinting();
        return Printable.PAGE_EXISTS;
    }

    /** For nodes, they can ask, whether or not the width must be bigger to prevent the "..." at the output. (Bug of java).*/
    public boolean isCurrentlyPrinting() { return isPrinting;};

    /**
     * Return the bounding box of all the descendants of the source view, that without BORDER.
     * Should that be implemented in LayoutManager as minimum size?
     */
    public Rectangle getInnerBounds() {
        final Rectangle innerBounds = new Rectangle(getRoot().getX(), getRoot().getY(), getRoot().getWidth(), getRoot().getHeight());        
        final Rectangle maxBounds = new Rectangle(0, 0, getWidth(), getHeight());        
        for(int i = 0; i < ArrowLinkViews.size(); ++i) {
            ArrowLinkView arrowView = (ArrowLinkView) ArrowLinkViews.get(i);
            Rectangle arrowViewBigBounds = arrowView.arrowLinkCurve.getBounds();
            if (! innerBounds.contains(arrowViewBigBounds)){
                Rectangle arrowViewBounds =PathBBox.getBBox(arrowView.arrowLinkCurve).getBounds();
                innerBounds.add(arrowViewBounds);
            }
        }
        return innerBounds.intersection(maxBounds);
    }

    
    public NodeView getRoot() {
        return rootView; }

    private MindMapLayout getMindMapLayout() {
        return (MindMapLayout)getLayout();  }

    /**
     * This method is a workaround to allow the inner class access to "this".
     * Change it as soon the correct syntax is known.
     */
    private MapView getMap() {
        return this; }

    public Controller getController() {
        return controller;  }

    // this property is used when the user navigates up/down using cursor keys (PN)
    // it will keep the level of nodes that are understand as "siblings"

    public int getSiblingMaxLevel() {
        return this.siblingMaxLevel;
    }
    public void setSiblingMaxLevel(int level) {
        this.siblingMaxLevel = level;
    }

    private static final int margin = 20;

    /* (non-Javadoc)
     * @see java.awt.dnd.Autoscroll#getAutoscrollInsets()
     */
    public Insets getAutoscrollInsets() {
        Rectangle outer = getBounds();
        Rectangle inner = getParent().getBounds();
        return new Insets(
        inner.y - outer.y + margin, inner.x - outer.x + margin,
        outer.height - inner.height - inner.y + outer.y + margin,
        outer.width - inner.width - inner.x + outer.x + margin);    }

    /* (non-Javadoc)
     * @see java.awt.dnd.Autoscroll#autoscroll(java.awt.Point)
     */
    public void autoscroll(Point cursorLocn) {
        Rectangle r = new Rectangle((int)cursorLocn.getX() - margin, (int)cursorLocn.getY() - margin, 1+ 2*margin, 1+ 2*margin);
        scrollRectToVisible(r);
    }
    
    public NodeView getNodeView(MindMapNode node){
        if(node == null){
            return null;
        }
        Collection viewers = node.getViewers();
        final Iterator iterator = viewers.iterator();
        while(iterator.hasNext()){
            NodeView candidateView =(NodeView) iterator.next();
            if(candidateView.getMap() == this){
                return candidateView;
            }
        }
        return null;
    }
    /**
     * @return Returns the rootContentLocation.
     */
    Point getRootContentLocation() {
        return rootContentLocation;
    }

    /* (non-Javadoc)
     * @see javax.swing.JComponent#getPreferredSize()
     */
    public Dimension getPreferredSize() {
        if(! getParent().isValid()){
            return getLayout().preferredLayoutSize(this);
        }
        return super.getPreferredSize();
    }
    
    

}