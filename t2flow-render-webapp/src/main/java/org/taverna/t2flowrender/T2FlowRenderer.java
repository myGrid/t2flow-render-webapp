package org.taverna.t2flowrender;

import static java.io.File.createTempFile;
import static java.nio.file.Files.readAllBytes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("webapp")
@Path("/")
public class T2FlowRenderer {
	public static final String T2FLOW = "application/vnd.taverna.t2flow+xml";
	public static final String SVG = "image/svg+xml";

	@Autowired
	private Invoker invoker;

	@GET
	@Produces("text/plain")
	public String describe() {
		return "This service renders a t2flow as SVG.\nPOST your t2flow as "
				+ T2FLOW + " and prepare to receive it as " + SVG
				+ " shortly after.";
	}

	@POST
	@Consumes(T2FLOW)
	@Produces(SVG)
	public byte[] renderT2flow(byte[] t2flow) {
		File src = null, dst = null;
		try {
			src = createTempFile("src", ".t2flow");
			dst = createTempFile("dst", ".svg");
			try (FileOutputStream fos = new FileOutputStream(src)) {
				fos.write(t2flow);
			}
			invoker.run(src, dst);
			return readAllBytes(dst.toPath());
		} catch (IOException | InterruptedException e) {
			throw new WebApplicationException(e, 500);
		} finally {
			if (src != null)
				src.delete();
			if (dst != null)
				dst.delete();
		}
	}
}
