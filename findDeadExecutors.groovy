def issue = false, dead = false, inactive=false, job_failure=false
// get handle to out
def config = new HashMap()
def bindings = getBinding()
config.putAll(bindings.getVariables())
def out = config['out']

for (aSlave in hudson.model.Hudson.instance.slaves) {
    if ( aSlave.getLabelString() == "release" ) {
        issue = false
        dead = false
        inactive=false
        lastBuild = aSlave.getComputer().getTimeline().getLastBuild();
        println('==========================================================')
        println("Name: ${aSlave.name}")
        println("\tcomputer.isOffline: ${aSlave.getComputer().isOffline()}")

        // check that a job had been run in the past hour
        Calendar oneHourAgo = Calendar.getInstance()
        oneHourAgo.add(Calendar.HOUR_OF_DAY, -1)
        if (lastBuild?.getTimestamp() < oneHourAgo) {
            println('\tLast build was: ' + lastBuild?.timestampString);
            inactive=true
        }
        
        if ( aSlave.getComputer().isOffline() ) {
            def cause = aSlave.getComputer().getOfflineCause().toString()
            println("\tcomputer.getOfflineCause  $cause" )
            if ( cause && !cause.contains("null") && !cause.contains( "idle" ) && !cause.contains( "Disconnected" )) {
                issue = true
            }
        } else {
            // check if executor is dead
            execList = aSlave.getComputer().getExecutors()
            for( exec in execList ) {
                if (exec.getCauseOfDeath()) {
                    dead=true
                    println("\tSlave ${aSlave.name} has a dead executor!!")
                    exec.getCauseOfDeath().printStackTrace(out)
                    println('\t================')
                    println("\tYanking Executor!")
                    exec.doYank()
                }
            }
        }

        def thr = Thread.currentThread()
        def build = thr?.executable
        if ( (issue&&inactive)||dead ) {
            build.setResult( hudson.model.Result.FAILURE  )
        } else {
            build.setResult( hudson.model.Result.SUCCESS  )
        }
    }
}
