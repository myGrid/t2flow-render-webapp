package org.taverna.t2flowrender;

import static java.io.File.createTempFile;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static javax.ws.rs.core.Response.serverError;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;
import static org.apache.commons.logging.LogFactory.getLog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;

import org.apache.commons.logging.Log;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.taverna.t2flowrender.Invoker.Format;

@Component("webapp")
@Path("/")
public class T2FlowRenderer {
	public static final String T2FLOW = "application/vnd.taverna.t2flow+xml";
	public static final String SVG = "image/svg+xml";
	public static final String PNG = "image/png";
	public static final String HTML = "text/html";
	public static final String FORM = "multipart/form-data";

	public static final String F_T2FLOW = "t2flow";
	public static final String F_WIDTH = "width";
	public static final String F_HEIGHT = "height";

	@Autowired
	private Invoker invoker;

	Log log = getLog(T2FlowRenderer.class);

	@GET
	@Produces(HTML)
	public String describeAndShowForm() {
		return "This service renders a t2flow as SVG.\nPOST your "
				+ "t2flow as <tt>" + T2FLOW
				+ "</tt> and prepare to receive it as <tt>" + SVG
				+ "</tt> or <tt>" + HTML
				+ "</tt> shortly after. (Content type negotiation will "
				+ "be used.)<p>" + "Alternatively, use this form to upload a "
				+ "t2flow file and get a little piece of HTML back."
				+ "<FORM ENCTYPE=\"multipart/form-data\" METHOD=POST>"
				+ "<INPUT TYPE=FILE NAME=" + F_T2FLOW + ">"
				+ "<INPUT TYPE=HIDDEN NAME=" + F_WIDTH + " VALUE=640>"
				+ "<!--<INPUT TYPE=HIDDEN NAME=" + F_HEIGHT + " VALUE=480>-->"
				+ "<INPUT TYPE=SUBMIT>" + "</FORM>";
	}

	@POST
	@Consumes(T2FLOW)
	@Produces(SVG)
	public byte[] renderT2flowAsSVG(InputStream t2flow) {
		File src = null, dst = null;
		try {
			src = createTempFile("src", ".t2flow");
			dst = createTempFile("dst", ".svg");
			copy(t2flow, src.toPath(), REPLACE_EXISTING);
			log.info("stored t2flow in " + src.toPath() + " (" + src.length()
					+ " bytes)");
			invoker.run(src, dst);
			log.info("converted to svg in " + dst.toPath() + " ("
					+ dst.length() + " bytes)");
			return readAllBytes(dst.toPath());
		} catch (IOException | InterruptedException e) {
			throw new WebApplicationException(e, serverError().entity(
					"problem when converting to image: " + e.getMessage())
					.build());
		} finally {
			if (src != null)
				src.delete();
			if (dst != null)
				dst.delete();
		}
	}

	@POST
	@Consumes(T2FLOW)
	@Produces(PNG)
	public byte[] renderT2flowAsPNG(InputStream t2flow) {
		File src = null, dst = null;
		try {
			src = createTempFile("src", ".t2flow");
			dst = createTempFile("dst", ".png");
			copy(t2flow, src.toPath(), REPLACE_EXISTING);
			log.info("stored t2flow in " + src.toPath() + " (" + src.length()
					+ " bytes)");
			invoker.run(src, dst, Format.PNG);
			log.info("converted to svg in " + dst.toPath() + " ("
					+ dst.length() + " bytes)");
			return readAllBytes(dst.toPath());
		} catch (IOException | InterruptedException e) {
			throw new WebApplicationException(e, serverError().entity(
					"problem when converting to image: " + e.getMessage())
					.build());
		} finally {
			if (src != null)
				src.delete();
			if (dst != null)
				dst.delete();
		}
	}

	@POST
	@Consumes(T2FLOW)
	@Produces(HTML)
	public String htmlT2flow(InputStream t2flow,
			@QueryParam("width") Integer width,
			@QueryParam("height") Integer height) {
		return renderToHtml(t2flow, width, height);
	}

	@POST
	@Consumes(FORM)
	@Produces(HTML)
	public String formT2flow(
			@Multipart(value = F_T2FLOW, required = true) InputStream t2flow,
			@Multipart(value = F_WIDTH, required = false) Integer width,
			@Multipart(value = F_HEIGHT, required = false) Integer height) {
		return renderToHtml(t2flow, width, height);
	}

	private String renderToHtml(InputStream t2flow, Integer width,
			Integer height) {
		File src = null, dst = null;
		try {
			src = createTempFile("src", ".t2flow");
			dst = createTempFile("dst", ".svg");
			copy(t2flow, src.toPath(), REPLACE_EXISTING);
			log.info("stored t2flow in " + src.toPath() + " (" + src.length()
					+ " bytes)");
			invoker.run(src, dst);
			log.info("converted to svg in " + dst.toPath() + " ("
					+ dst.length() + " bytes)");
			StringBuilder sb = new StringBuilder("<img ");
			if (width != null)
				sb.append("width=").append(width).append("px ");
			if (height != null)
				sb.append("height=").append(height).append("px ");
			sb.append("src=\"data:" + SVG + ";base64,");
			sb.append(printBase64Binary(readAllBytes(dst.toPath())));
			return sb.append("\" />").toString();
		} catch (IOException | InterruptedException e) {
			throw new WebApplicationException(e, serverError().entity(
					"problem when converting to image: " + e.getMessage())
					.build());
		} finally {
			if (src != null)
				src.delete();
			if (dst != null)
				dst.delete();
		}
	}
}
