package DAG;

import java.io.FileInputStream;
import java.io.FileNotFoundException;



import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import DAG.*;

public class DagUtils {
	public static  void marshall(DAG.Adag dn,String filename) throws JAXBException {
		JAXBContext context= JAXBContext.newInstance(DAG.Adag.class);
		Marshaller mar = context.createMarshaller();
		mar.marshal(dn, new File(filename));
	}
	public static  DAG.Adag unmarshall(String filename) throws JAXBException {
		JAXBContext context= JAXBContext.newInstance(DAG.Adag.class);
		Unmarshaller umar= context.createUnmarshaller();
		DAG.Adag unmarshalled= (DAG.Adag)umar.unmarshal(new File(filename));
	
	//	DAG.Adag unmarshalled=unmarshall(filename);
		
		return unmarshalled;
	}
	
	public static Adag readWorkflowDescription(String wfdescFile) throws JAXBException
	{
		try
		{
			DAG.Adag dd= unmarshall(wfdescFile);
			return dd;

		}
		catch (JAXBException e1)
		{
			System.out.println("JAXB exception " + e1.getMessage());
			throw e1;
		}

	}
	
	

}
