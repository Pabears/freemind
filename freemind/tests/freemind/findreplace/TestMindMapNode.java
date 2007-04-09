/*FreeMind - A Program for creating and viewing Mindmaps
 *Copyright (C) 2006  Christian Foltin <christianfoltin@users.sourceforge.net>
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
/*$Id: TestMindMapNode.java,v 1.1.2.10.2.1 2007-04-09 11:43:37 dpolivaev Exp $*/

package tests.freemind.findreplace;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.SortedMap;

import javax.swing.ImageIcon;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import freemind.controller.filter.FilterInfo;
import freemind.extensions.NodeHook;
import freemind.extensions.PermanentNodeHook;
import freemind.main.XMLElement;
import freemind.main.Tools.BooleanHolder;
import freemind.modes.HistoryInformation;
import freemind.modes.MindIcon;
import freemind.modes.MindMap;
import freemind.modes.MindMapCloud;
import freemind.modes.MindMapEdge;
import freemind.modes.MindMapLinkRegistry;
import freemind.modes.MindMapNode;
import freemind.modes.ModeController;
import freemind.modes.TreeModelListener;
import freemind.modes.attributes.Attribute;
import freemind.modes.attributes.NodeAttributeTableModel;
import freemind.view.mindmapview.NodeView;

final class TestMindMapNode implements MindMapNode {
    private String text = "";
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getObjectId(ModeController controller) {
        return null;
    }

    public ListIterator childrenFolded() {
        return null;
    }

    public ListIterator childrenUnfolded() {
        return null;
    }

    public boolean hasChildren() {
        return false;
    }

    public FilterInfo getFilterInfo() {
        return null;
    }

    public int getChildPosition(MindMapNode childNode) {
        return 0;
    }

    public MindMapNode getPreferredChild() {
        return null;
    }

    public void setPreferredChild(MindMapNode node) {
    }

    public int getNodeLevel() {
        return 0;
    }

    public String getLink() {
        return null;
    }

    public String getShortText(ModeController controller) {
        return null;
    }

    public MindMapEdge getEdge() {
        return null;
    }

    public Color getColor() {
        return null;
    }

    public String getStyle() {
        return null;
    }

    public void setStyle(String style) {
    }

    public MindMapNode getParentNode() {
        return null;
    }

    public boolean isBold() {
        return false;
    }

    public boolean isItalic() {
        return false;
    }

    public boolean isUnderlined() {
        return false;
    }

    public Font getFont() {
        return null;
    }

    public String getFontSize() {
        return null;
    }

    public String getFontFamilyName() {
        return null;
    }

    public NodeView getViewer() {
        return null;
    }

    public void setViewer(NodeView viewer) {
    }

    public String getPlainTextContent() {
        return null;
    }

    public TreePath getPath() {
        return null;
    }

    public boolean isDescendantOf(MindMapNode node) {
        return false;
    }

    public boolean isRoot() {
        return false;
    }

    public boolean isFolded() {
        return false;
    }

    public BooleanHolder isLeft() {
        return null;
    }

    public boolean isOnLeftSideOfRoot() {
        return false;
    }

    public void setLeft(boolean isLeft) {
    }

    public void setFolded(boolean folded) {
    }

    public void setFont(Font font) {
    }

    public void setShiftY(int y) {
    }

    public int getShiftY() {
        return 0;
    }

    public int calcShiftY() {
        return 0;
    }

    public void setVGap(int i) {
    }

    public int getVGap() {
        return 0;
    }

    public int calcVGap() {
        return 0;
    }

    public void setHGap(int i) {
    }

    public int getHGap() {
        return 0;
    }

    public void setLink(String link) {
    }

    public void setFontSize(int fontSize) {
    }

    public void setColor(Color color) {
    }

    public List getIcons() {
        return null;
    }

    public void addIcon(MindIcon icon) {
    }

    public int removeLastIcon() {
        return 0;
    }

    public MindMapCloud getCloud() {
        return null;
    }

    public void setCloud(MindMapCloud cloud) {
    }

    public Color getBackgroundColor() {
        return null;
    }

    public void setBackgroundColor(Color color) {
    }

    public List getHooks() {
        return null;
    }

    public Collection getActivatedHooks() {
        return null;
    }

    public PermanentNodeHook addHook(PermanentNodeHook hook) {
        return null;
    }

    public void invokeHook(NodeHook hook) {
    }

    public void removeHook(PermanentNodeHook hook) {
    }

    public void setToolTip(String key, String tip) {
    }

    public SortedMap getToolTip() {
        return null;
    }

    public void setAdditionalInfo(String info) {
    }

    public String getAdditionalInfo() {
        return null;
    }

    public MindMapNode shallowCopy() {
        return null;
    }

    public XMLElement save(Writer writer, MindMapLinkRegistry registry, boolean saveHidden, boolean saveChildren) throws IOException {
        return null;
    }

    public Map getStateIcons() {
        return null;
    }

    public void setStateIcon(String key, ImageIcon icon) {
    }

    public HistoryInformation getHistoryInformation() {
        return null;
    }

    public void setHistoryInformation(HistoryInformation historyInformation) {
    }

    public boolean isVisible() {
        return false;
    }

    public boolean hasOneVisibleChild() {
        return false;
    }

    public MindMap getMap() {
        return null;
    }

    public NodeAttributeTableModel getAttributes() {
        return null;
    }

    public void addTreeModelListener(TreeModelListener l) {
    }

    public void removeTreeModelListener(TreeModelListener l) {
    }

    public void insert(MutableTreeNode child, int index) {
    }

    public void remove(int index) {
    }

    public void remove(MutableTreeNode node) {
    }

    public void setUserObject(Object object) {
    }

    public void removeFromParent() {
    }

    public void setParent(MutableTreeNode newParent) {
    }

    public TreeNode getChildAt(int childIndex) {
        return null;
    }

    public int getChildCount() {
        return 0;
    }

    public TreeNode getParent() {
        return null;
    }

    public int getIndex(TreeNode node) {
        return 0;
    }

    public boolean getAllowsChildren() {
        return false;
    }

    public boolean isLeaf() {
        return false;
    }

    public Enumeration children() {
        return null;
    }

    public String getXmlText() {
        return null;
    }

    public void setXmlText(String structuredText) {
    }

	public String getXmlNoteText() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setXmlNoteText(String structuredNoteText) {
		// TODO Auto-generated method stub
		
	}

    public List getChildren() {
        return null;
    }

    public String getNoteText() {
        return null;
    }

    public void setNoteText(String noteText) {
    }

    public Attribute getAttribute(int pPosition) {
        return null;
    }

    public List getAttributeKeyList() {
        return null;
    }

    public boolean isAttributeExisting(String key) {
        return false;
    }

    public void setAttribute(int pPosition, Attribute pAttribute) {
    }

    public int getAttributeTableLength() {
        return 0;
    }

}
