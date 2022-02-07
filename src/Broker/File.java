package Broker;

import java.util.ArrayList;
import java.util.List;

import DAG.FileType;
import DAG.FilenameType;
import DAG.LinkageType;

public class File {
	public long fileSize;
	public String fileName;
	//public FileType fileType;
	public List<String> nodes = new ArrayList();
	public LinkageType link;
	
	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public File(long fileSize, String fileName, LinkageType links,Boolean test) {
		super();
		this.fileSize = fileSize;
		this.fileName = fileName;
		this.link = links;
	}

	public File(long fileSize, String fileName, List<String> nodes) {
		super();
		this.fileSize = fileSize;
		this.fileName = fileName;
		this.nodes = nodes;
	}
	
	


//	public FileType getFileType() {
//		return fileType;
//	}
//
//	public void setFileType(FileType fileType) {
//		this.fileType = fileType;
//	}

	public File(long fileSize, String fileName, LinkageType fileType) {
		super();
		this.fileSize = fileSize;
		this.fileName = fileName;
		this.link=fileType;
	//	this.type = fileType.en;
	}


	public LinkageType getLink() {
		return link;
	}

	public void setLink(LinkageType link) {
		this.link = link;
	}

	public void AddNode(String nodeId) {
		if (!nodes.contains(nodeId))
			nodes.add(nodeId);
	}

	public String pullNode() {
		if (nodes.size() > 0)
			return nodes.get(nodes.size() - 1);
		return null;
	}

}
