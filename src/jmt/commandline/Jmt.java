package jmt.commandline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jmt.analytical.SolverDispatcher;
import jmt.common.exception.LoadException;
import jmt.common.exception.NetException;
import jmt.engine.simDispatcher.Dispatcher_jSIMschema;
import jmt.gui.common.xml.XMLArchiver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class Jmt {
	
	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
	    TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer = tf.newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	    transformer.transform(new DOMSource(doc), 
	         new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}
	
	public static void help(){
		System.err.println("Usage: jmt.commandline.Jmt [sim|mva] [modelfilename]");
		System.exit(2);
	}
	
	public static void copyFile(File sourceFile, File destFile) throws IOException {
		 if(!destFile.exists()) {
		  destFile.createNewFile();
		 }

		 FileChannel source = null;
		 FileChannel destination = null;
		 try {
		  source = new FileInputStream(sourceFile).getChannel();
		  destination = new FileOutputStream(destFile).getChannel();

		   // previous code: destination.transferFrom(source, 0, source.size());
		   //should be to avoid infinite loops.

		  long count =0;
		  long size = source.size();                
		  while((count += destination.transferFrom(source, 0, size-count))<size);
		 }
		 finally {
		  if(source != null) {
		   source.close();
		  }
		  if(destination != null) {
		   destination.close();
		  }
		}
	}
	
	public static void main(String[] args) throws LoadException, NetException, Exception {
		if(args.length < 2){
			help();
		}
		if(args[0].equals("mva")){
			File model = new File(args[1]);
			File result = new File(args[1]+"-result.jmva");
			
			if (!model.isFile()) {
				System.err.print("Invalid model file: " + model.getAbsolutePath());
				System.exit(1);
			}

			SolverDispatcher dispatcher = new SolverDispatcher();
			
			// Starts the solution
			dispatcher.solve(model);
			copyFile(model, result);			

		} else if(args[0].equals("sim")){
			
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			File model = new File(args[1]);
			System.out.println(args[1]);
			File temp = File.createTempFile("tempfileSim",".jsim");
			temp.deleteOnExit();
			
			if (!model.isFile()) {
				System.err.print("Invalid model file: " + model.getAbsolutePath());
				System.exit(1);
			}
			
			Document doc = db.parse(model);
			Element sim = XMLArchiver.getSimFromArchiveDocument(doc);
			
			Document doc2 = db.newDocument();
			Node dup = doc2.importNode(sim, true);
			NamedNodeMap attributes = dup.getAttributes();
			attributes.removeNamedItem("xsi:noNamespaceSchemaLocation");
			doc2.appendChild(dup);
			
			/*
			 * save to a temp file
			 */
			TransformerFactory tranFactory = TransformerFactory.newInstance();  
			Transformer aTransformer = tranFactory.newTransformer();  
			Source src = new DOMSource(doc2);  
			Result dest = new StreamResult(temp);  
			aTransformer.transform(src, dest);  
			
			Dispatcher_jSIMschema dispatcher = new Dispatcher_jSIMschema(temp);
			// Sets simulation seed if required
			if (args.length > 2) {
				try {
					dispatcher.setSimulationSeed(Long.parseLong(args[2]));
				} catch (NumberFormatException ex) {
					System.err.println("Invalid simulation seed");
					System.exit(1);
				}
			}
			
			// Starts the simulation
			dispatcher.solveModel();
			
			File output = dispatcher.getOutputFile();
			
			File result = new File(args[1]+"-result.jsim");
			
			copyFile(output, result);
			output.delete();

		} else {
			help();
		}
	}
}
