package jip.utils

import jip.jobs.DefaultJob
import jip.jobs.DefaultPipelineJob
import org.junit.Test

/**
 * 
 * @author Thasso Griebel <thasso.griebel@gmail.com>
 */
class TemplatesTest {

    @Test
    public void testJobScriptTemplate() throws Exception {
        def pipelineJob = new DefaultPipelineJob("1")
        def job = new DefaultJob("1", "2", ".")
        pipelineJob.getJobs().add(job)
        def script = Templates.toJobScript(job, "test")
        print "Script:${script}"
        assert script == """#!/bin/bash
#
# JIP job script
#

JIP_PIPELINE=1
JIP_JOB=2

jip.info(){
    jip message -p \${JIP_PIPELINE} -j \${JIP_JOB} --info \$@
}

jip.warn(){
    jip message -p \${JIP_PIPELINE} -j \${JIP_JOB} --warn \$@
}

jip.error(){
    jip message -p \${JIP_PIPELINE} -j \${JIP_JOB} --error \$@
}

jip.progress(){
    jip message -p \${JIP_PIPELINE} -j \${JIP_JOB} --progress \$1
}

test"""

    }
}
