package bg.softuni.servlet.ipfs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import io.ipfs.api.IPFS;
import io.ipfs.api.MerkleNode;
import io.ipfs.api.NamedStreamable;
import io.ipfs.multihash.Multihash;

@WebServlet("/ipfs")
@MultipartConfig
public class IPFSServlet extends HttpServlet{

	private IPFS ipfs; 
	
	@Override
	public void init() {
	     ipfs = new IPFS("/ip4/127.0.0.1/tcp/5001");
	     try {
			ipfs.refs.local();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    Part filePart = request.getPart("file"); 
	    String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString(); 
	    InputStream fileContent = filePart.getInputStream();
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();

	    int nRead;
	    byte[] data = new byte[16384];
	    while ((nRead = fileContent.read(data, 0, data.length)) != -1) {
	      buffer.write(data, 0, nRead);
	    }
	    buffer.flush();
	   
	    //Todo check
	    //FileOutputStream fos = new FileOutputStream(fileName);
	    //fos.write(buffer.toByteArray());
	    //fos.close();
	    
	    NamedStreamable.ByteArrayWrapper byteArrayWrapper = new NamedStreamable.ByteArrayWrapper(fileName, buffer.toByteArray());
	    MerkleNode result = ipfs.add(byteArrayWrapper).get(0);
	    
	    PrintWriter writer = response.getWriter();
	    writer.println(result.hash);
	    writer.flush();
	}
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException  {
		String hash = req.getParameter("hash");
		if(hash!=null){
			Multihash filePointer = Multihash.fromBase58(hash);
			byte[] fileContents = ipfs.cat(filePointer);
			OutputStream out = res.getOutputStream();
			out.write(fileContents, 0, fileContents.length);
			out.flush();
		}else{
			
		}
	}
	
}