#! /usr/bin/python

__author__="ylh"
__date__ ="$2009-4-14 10:43:56$"


import os
import re
import sys
import pickle
import parser
import os.path
import time
from datetime import datetime
from datetime import date
from mysqlconfig import MysqlConfig
from optparse import OptionParser


class Statictician:
		def __init__(self, output, mysqlConfig, engine, tables=[]):
				self.mysqlConfig = mysqlConfig
				self.eng = engine
				self.tables = tables
				self.output = output
				self.outputPath = output + "/statistician-" + engine
				if False == os.path.exists(self.outputPath):
				    if False == os.path.exists(output):
				    		os.mkdir(output)
				    os.mkdir(self.outputPath)
				data = self.executesql("show tables like 'NTSE%';", dbname="information_schema")
				metaTabs = data.splitlines()
				self.metaTabs = metaTabs[1:]
				self.metaTabs.sort()
				
		def start(self, iter):
				self.getRunInfo(1, "before")
				self.getdbvar(True)
				self.getTblStats(True)
								
				self.getRunInfo(iter, "run")
                #self.executecmd("cp " + os.path.join(self.mysqlConfig.mysqlInstallPath, "my.cnf") + " .")
				f = open(self.getfullpath("meta"), 'w')
				str = "mysqlversion: %(version)s\n"
				f.write(str % {"version": self.mysqlConfig.version})
				f.close()

		def getdbvar(self, beforeTest):
				if not beforeTest:
						suffix = "after"
                        #"show status first" when test is done
						cmd = "show variables like '" + self.eng + "%';"
                        #cmd = "show global variables;"
						self.executesql(cmd, "variables." + suffix)
				else:
						suffix = "before"
        
				if beforeTest:
						# "show status at end " before run test
						cmd = "show variables like '" + self.eng + "%';"
						#cmd = "show global variables;"
						self.executesql(cmd, "variables." + suffix)

		def getTblStats(self, beforeTest):
				if not beforeTest:
						suffix = "after"
				else:
						suffix = "before"
            
				for (db, tabs) in self.tables:
						cmd = "show table status like '%(table)s'";
						for tab in tabs:
								sql = cmd % {"table": tab}
								self.executesql(sql, db + "." + tab + ".status." + suffix, dbname = db)
				
		def getsubdbstats(self, file, period):
				file.writelines(["###########################################################", "\n"])
				#file.writelines(["=========================Test %s===========================" % (period), "\n"])
				dt=datetime.now()
				file.writelines(["#%s" % (dt.strftime('%Y-%m-%d %H:%M:%S')), "\n"])
				file.writelines(["###########################################################", "\n\n"])
				cmd = "show status like '" + self.eng + "%';"
				file.write(self.executesql(cmd)) 		

		def getRunInfo(self, iter, suffix):
				f1 = open(self.getfullpath("information_schema." + suffix), "w")
				f2 = open(self.getfullpath("status." + suffix), "w")
				for i in range(0,iter):
					self.getsubdbstats(f2, i)
					if i % 5 == 0:
							self.colloct_infor_schema(f1, i)
					if iter - 1 != i: 
							time.sleep(60)
				f1.close()
				f2.close()
				            
		def colloct_infor_schema(self, file, period):
				file.writelines(["###########################################################", "\n"])
				#file.writelines(["=========================Test %s===========================" % (period), "\n"])
				dt=datetime.now()
				file.writelines(["#%s" % (dt.strftime('%Y-%m-%d %H:%M:%S')), "\n"])
				file.writelines(["###########################################################", "\n\n"])
				r = re.compile('NTSE_[\w]*_+EX')
				for tab in self.metaTabs:
						m = r.search(tab)
						if m != None:
							continue
						file.writelines(["----------------", tab, "----------------", "\n"])
						file.write(self.executesql("select * from " + tab, dbname="information_schema"))
						file.write("\n\n\n")

		def executesql(self, sql, outfilename = "", dbname = ""):
				if outfilename != "":
						outfilename = self.getfullpath(outfilename);
				return self.mysqlConfig.executesql(sql, outfilename, dbname);

		def executecmd(self, cmd):
				print cmd
				os.system(cmd)

		def getfullpath(self, filename):
				return os.path.join(self.outputPath, filename)

		def stop(self):
				self.getRunInfo(1, "after")
				self.getdbvar(False)
				self.getTblStats(False)

		def clean(self):
				self.executecmd("rm -rf " + self.outputPath)


class StatisticianRunner:
		@staticmethod
		def createTest(output, mysqlConfig, engine, tables = []):
				worker = Statictician(output, mysqlConfig, engine, tables)
				StatisticianRunner.save(output, worker)

		@staticmethod
		def start(output, iter):
				worker = StatisticianRunner.load(output)
				worker.start(iter);

		@staticmethod
		def stop(output):
				worker = StatisticianRunner.load(output)
				worker.stop()

		@staticmethod
		def clean(output):
				worker = StatisticianRunner.load(output)
				worker.clean()

		@staticmethod
		def drop(output):
				worker = StatisticianRunner.load(output)
                #worker.clean()
				filename = output + '/Statictician.run'
				if os.path.exists(filename):
						os.remove(filename)

		@staticmethod
		def save(output, worker):
				output = open(output + '/Statictician.run', 'wb')
				pickle.dump(worker, output)
				output.close()

		@staticmethod
		def load(output):
				input = open(output + '/Statictician.run', 'rb')
				worker = pickle.load(input)
				input.close()
				return worker


def runBatch():
    parser = OptionParser()
    parser.set_usage(parser.get_usage().rstrip() + " {create | start | stop | clean | drop}")
    parser.add_option("-t", "--tables", dest="tables", type="string", default="test: Blog"
        , help = "format: \"dbname1: table1 table2;dbname2: table3;\"")
    parser.add_option("-m", "--home", dest="mysqlhome", type="string", default="/usr"
        , help = "mysql home directory")
    parser.add_option("-H", "--host", dest="mysqlhost", type="string", default="127.0.0.1"
        , help = "mysql server address")
    parser.add_option("-P", "--port", dest="mysqlport", type="int", default = 3306
        , help = "mysql server port")
    parser.add_option("-u", "--user", dest="mysqluser", type="string", default = "root"
        , help = "mysql server user name")
    parser.add_option("-p", "--password", dest="mysqlpassword", type="string", default=""
        , help = "mysql server password")
    parser.add_option("-e", "--engine", dest="engine", type="string", default="innodb"
    		, help = "mysql storage engine")
    parser.add_option("-i", "--iteration", dest="iteration", type=int
    		,	help = "the iteration times of query information_schemation database")
    parser.add_option("-o", "--output", dest="output", type="string", default="./"
    		, help = "output directory for result")
    
    (options, args) = parser.parse_args()
    
    tables = []
    if options.tables != None:
        options.tables.strip(";")
        for dbtabs in options.tables.split(";"):
            str = dbtabs.split(":")
            print str
            tables.append((str[0], str[1].split()))
    if len(args) <= 0:
        parser.print_usage()
        return

    if args[0] == "create":
        StatisticianRunner.createTest(options.output, MysqlConfig(options.mysqlhome, options.mysqlhost
            , options.mysqlport, options.mysqluser, options.mysqlpassword), options.engine, tables)
    elif args[0] == "start":
        if options.iteration == None:
    		parser.print_usage()
    		return
        StatisticianRunner.start(options.output, options.iteration)
    elif args[0] == "stop":
        StatisticianRunner.stop(options.output)
    elif args[0] == "clean":
        StatisticianRunner.clean(options.output)
    elif args[0] == "drop":
        StatisticianRunner.drop(options.output)
    else:
        parser.print_usage()
        return

if __name__ == "__main__":
    runBatch()
