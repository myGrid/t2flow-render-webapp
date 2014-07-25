package org.taverna.t2flowrender;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class Invoker {
	@Value("classpath:/t2flow_render.rb")
	private Resource rfile;

	public void run(File src, File dst) throws IOException,
			InterruptedException {
		String rbfile = rfile.getFile().toString();
		ProcessBuilder pb = new ProcessBuilder("ruby", rbfile, src.toString(),
				dst.toString());
		Process p = pb.start();
		p.waitFor();
	}
}
