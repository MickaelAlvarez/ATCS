package com.gpl.rpg.atcontentstudio.model.maps;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.TreeNode;

import com.gpl.rpg.atcontentstudio.model.GameSource;
import com.gpl.rpg.atcontentstudio.model.GameSource.Type;
import com.gpl.rpg.atcontentstudio.model.Project;
import com.gpl.rpg.atcontentstudio.model.ProjectTreeNode;
import com.gpl.rpg.atcontentstudio.model.gamedata.GameDataSet;
import com.gpl.rpg.atcontentstudio.ui.DefaultIcons;

public class TMXMapSet implements ProjectTreeNode {

	public static final String DEFAULT_REL_PATH_IN_SOURCE = "res/xml/";
	public static final String DEFAULT_REL_PATH_IN_PROJECT = "maps/";

	public File mapFolder = null;
	public List<TMXMap> tmxMaps;
	
	public ProjectTreeNode parent;
	
	public TMXMapSet(GameSource source) {
		this.parent = source;
		if (source.type == GameSource.Type.source) this.mapFolder = new File(source.baseFolder, DEFAULT_REL_PATH_IN_SOURCE);
		else if (source.type == GameSource.Type.created | source.type == GameSource.Type.altered) {
			this.mapFolder = new File(source.baseFolder, DEFAULT_REL_PATH_IN_PROJECT);
			if (!this.mapFolder.exists()) {
				this.mapFolder.mkdirs();
			}
		}
		this.tmxMaps = new ArrayList<TMXMap>();
		
		if (this.mapFolder != null) {
			for (File f : this.mapFolder.listFiles()) {
				if (f.getName().endsWith(".tmx") || f.getName().endsWith(".TMX")) {
					TMXMap map = new TMXMap(this, f);
					if (source.type == GameSource.Type.created | source.type == GameSource.Type.altered) {
						map.writable = true;
					}
					tmxMaps.add(map);
				}
			}
		}
	}
	
	@Override
	public Enumeration<TMXMap> children() {
		return Collections.enumeration(tmxMaps);
	}
	@Override
	public boolean getAllowsChildren() {
		return true;
	}
	@Override
	public TreeNode getChildAt(int arg0) {
		return tmxMaps.get(arg0);
	}
	@Override
	public int getChildCount() {
		return tmxMaps.size();
	}
	@Override
	public int getIndex(TreeNode arg0) {
		return tmxMaps.indexOf(arg0);
	}
	@Override
	public TreeNode getParent() {
		return parent;
	}
	@Override
	public boolean isLeaf() {
		return false;
	}
	@Override
	public void childrenAdded(List<ProjectTreeNode> path) {
		path.add(0, this);
		parent.childrenAdded(path);
	}
	@Override
	public void childrenChanged(List<ProjectTreeNode> path) {
		path.add(0, this);
		parent.childrenChanged(path);
	}
	@Override
	public void childrenRemoved(List<ProjectTreeNode> path) {
		if (path.size() == 1 && this.getChildCount() == 1) {
			childrenRemoved(new ArrayList<ProjectTreeNode>());
		} else {
			path.add(0, this);
			parent.childrenRemoved(path);
		}
	}
	@Override
	public void notifyCreated() {
		childrenAdded(new ArrayList<ProjectTreeNode>());
		for (TMXMap map : tmxMaps) {
			map.notifyCreated();
		}
	}
	@Override
	public String getDesc() {
		return "TMX Maps";
	}
	
	@Override
	public Project getProject() {
		return parent.getProject();
	}
	

	@Override
	public Image getIcon() {
		return getOpenIcon();
	}
	@Override
	public Image getClosedIcon() {
		return DefaultIcons.getTmxClosedIcon();
	}
	@Override
	public Image getLeafIcon() {
		return DefaultIcons.getTmxClosedIcon();
	}
	@Override
	public Image getOpenIcon() {
		return DefaultIcons.getTmxOpenIcon();
	}
	
	@Override
	public GameDataSet getDataSet() {
		return null;
	}
	
	@Override
	public Type getDataType() {
		return parent.getDataType();
	}
	
	@Override
	public boolean isEmpty() {
		return tmxMaps.isEmpty();
	}

	public TMXMap getMap(String id) {
		if (tmxMaps == null) return null;
		for (TMXMap map : tmxMaps) {
			if (id.equals(map.id)){
				return map;
			}
		}
		return null;
	}

	public void addMap(TMXMap node) {
		ProjectTreeNode higherEmptyParent = this;
		while (higherEmptyParent != null) {
			if (higherEmptyParent.getParent() != null && ((ProjectTreeNode)higherEmptyParent.getParent()).isEmpty()) higherEmptyParent = (ProjectTreeNode)higherEmptyParent.getParent();
			else break;
		}
		if (higherEmptyParent == this && !this.isEmpty()) higherEmptyParent = null;
		tmxMaps.add(node);
		if (node.tmxFile != null) {
			//Altered node.
			node.tmxFile = new File(this.mapFolder, node.tmxFile.getName());
		} else {
			//Created node.
			node.tmxFile = new File(this.mapFolder, node.id+".tmx");
		}
		node.parent = this;
		if (higherEmptyParent != null) higherEmptyParent.notifyCreated();
		else node.notifyCreated();
	}

	public TMXMap get(int index) {
		return tmxMaps.get(index);
	}
	
}
