package Broker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import DAG.FilenameType;
import DAG.Adag.Job;

public class FileSets {
	public static List<File> FileSet = new ArrayList();
	public static List<File> FileSetByType = new ArrayList();
	
	public static void Insert(FilenameType file, Job job)
	{
		File fl = new File(Long.valueOf(file.getSize()), file.getFile(), file.getLink());
		//fl.AddNode(job.getId());
		String jobid=job.getId();
		int index=fileIndex(fl);
		if (index==-1)
		{
			fl.AddNode(jobid);
			FileSet.add(fl);
		}
		else
		{
			//String JobID=FileSet.get(index).pullNode();
			FileSet.get(index).AddNode(jobid);
		}
		File fl2 = new File(Long.valueOf(file.getSize()), file.getFile(), file.getLink());
		int index2=fileIndexTypeSensitive(fl2);
		if (index2==-1)
		{
			fl2.AddNode(jobid);
			FileSetByType.add(fl2);
		}
		else
		{
			//String JobID=FileSet.get(index).pullNode();
			FileSetByType.get(index2).AddNode(jobid);
		}
	}
	public static File returnAsFile(FilenameType file)
	{
		File fl = new File(Long.valueOf(file.getSize()), file.getFile(), file.getLink());
		return fl;
	}
	public static int Size()
	{
		return FileSet.size();
	}
	public static File get(int i)
	{
		return FileSet.get(i);
	}
	
	public static File getByType(int i)
	{
		return FileSetByType.get(i);
	}
	public static int SizeByType()
	{
		return FileSetByType.size();
	}
	
//	public static void AddFile(File fl,String NodeId)
//	{
//		int index=fileIndex(fl);
//		if (index==-1)
//		{
//			FileSet.add(fl);
//		}
//		else
//		{
//			String JobID=FileSet.get(index).pullNode();
//			FileSet.get(index).AddNode(JobID);
//		}
//		
//	}

	public static int fileIndex(File fl) {
		for (int i = 0; i < FileSet.size(); i++) {
			if (fl.getFileName().contains(FileSet.get(i).getFileName()))
				return i;
		}
		return -1;
	}
	public static int fileIndexTypeSensitive(File fl) {
		for (int i = 0; i < FileSetByType.size(); i++) {
			if (fl.getFileName().contains(FileSetByType.get(i).getFileName()) && fl.getLink()==FileSetByType.get(i).getLink())
				return i;
		}
		return -1;
	}

}
