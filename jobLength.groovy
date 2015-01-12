import hudson.model.*

calendar = new GregorianCalendar()
currentDateMillis = calendar.getTimeInMillis()
calendar.add(Calendar.DATE,-1)
yesterdayDateMillis = calendar.getTimeInMillis()
ave = 0

println('**********************************************')
ave = descendFolder(Hudson.instance.items, '/', ave)
println('**********************************************\n')


def totalJobsFromInterval(job){ job.getBuilds().byTimestamp(yesterdayDateMillis,currentDateMillis).size() }
def durationFromInterval(job){
    duration = 0
    for (hudson.model.Run build: job.getBuilds().byTimestamp(yesterdayDateMillis,currentDateMillis)){
       duration += build.getDuration()
    }
    duration
}

def descendFolder(items, path, ave) {
    items.each() { item ->
        if (item.class == hudson.model.FreeStyleProject) {
           if(item.isBuildable()) {
              duration = durationFromInterval(item)
              total = totalJobsFromInterval(item)
                if (duration > 0 && total >0) {
                 ave = (duration/total)/1000
                 println(path+item.name +' : ' + ave )
                   } else {
                     0
                   }
           }
        } else {
            ave = descendFolder(item.items,path+item.name+'/',ave)        
        }   
    }   
    ave
}
