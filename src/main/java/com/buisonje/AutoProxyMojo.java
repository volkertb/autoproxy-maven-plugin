package com.buisonje;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;

/**
 * Goal which automatically detects and configures the current system proxy server.
 *
 */
@Mojo(name = "detectProxy", defaultPhase = LifecyclePhase.VALIDATE)
public class AutoProxyMojo
    extends AbstractMojo
{

    /**
     * The Maven Settings.
     */
	@Parameter(defaultValue="${settings}", readonly=false)
    private Settings settings;

    @Override
    public void execute()
        throws MojoExecutionException
    {
    	
    	//FIXME
    	boolean throwException = false;
    	
    	if (throwException) {
    		throw new MojoExecutionException( "This is how you should respond with an error (optionally a cause exception as a second argument).");
    	}
    	
    	getLog().error( "Autodetecting system proxy server..." );

    	final Proxy fallbackProxy = settings.getActiveProxy();
    	
    	//TODO: obtain proxy server from Proxy Vole.
    	
    }
}
