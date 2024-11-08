package com.kiloflyers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.Session;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;

@Configuration
public class TensorFlowConfig {

	@Bean
	public Session tensorflowSession() throws IOException {
		Graph graph = new Graph();

		// Load the model from resources folder
		Resource resource = new ClassPathResource("models/deeplabv3_model.pb");

		graph.importGraphDef(Files.readAllBytes(Paths.get(resource.getURI())));

		// Return a new TensorFlow session
		return new Session(graph);
	}
}
