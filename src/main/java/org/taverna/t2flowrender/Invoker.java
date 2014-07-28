package org.taverna.t2flowrender;

import static org.apache.cxf.helpers.IOUtils.readStringFromStream;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class Invoker {
	@Value("classpath:/t2flow_render.rb")
	private Resource rfile;

	public static enum Format {
		SVG, PNG
	}

	public void run(File src, File dst) throws IOException,
			InterruptedException {
		String rbfile = rfile.getFile().toString();
		ProcessBuilder pb = new ProcessBuilder("ruby", rbfile, src.toString(),
				dst.toString());
		Process p = pb.start();
		if (p.waitFor() != 0)
			throw new IOException("problem in Ruby code: "
					+ readStringFromStream(p.getErrorStream()));
	}

	public void run(File src, File dst, Format format) throws IOException,
			InterruptedException {
		String rbfile = rfile.getFile().toString();
		ProcessBuilder pb = new ProcessBuilder("ruby", rbfile, src.toString(),
				dst.toString(), format.toString().toLowerCase());
		Process p = pb.start();
		if (p.waitFor() != 0)
			throw new IOException("problem in Ruby code: "
					+ readStringFromStream(p.getErrorStream()));
	}
}
