#! /usr/bin/python
#
# Copyright (c) <2011>, <NetEase Corporation>
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
#
# 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
# 3. Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#

__author__="LI WEIZHAO"
__contact__="liweizhao@corp.netease.com"
__date__ ="$2009-4-14 10:43:56$"


import os
import re
import sys
import pickle
import parser
import os.path
import subprocess
import time
import ConfigParser
import platform
import signal
import zipfile
import copy
from os.path import join
from datetime import datetime
from datetime import date
from optparse import OptionParser

class Util:
    @staticmethod
    def zipfolder(foldername,filename):
        '''
        zip a folder
        '''
        empty_dirs=[]
        zip=zipfile.ZipFile(filename,'w',zipfile.ZIP_DEFLATED)
        for root,dirs,files in os.walk(foldername):
            empty_dirs.extend([dir for dir in dirs if os.listdir(join(root,dir))==[]])
            for filename in files:
                #print "compressing",join(root,filename)
                zip.write(join(root,filename))
        for dir in empty_dirs:
            zif=zipfile.ZipInfo(root + '/')
            zip.writestr(zif,"")
        zip.close()

    @staticmethod
    def exeCmd(args, showCmd = True, showOutput = True):
        '''
        execute a command
        '''
        if showCmd:
            s = ' '.join(args)
            print s
        p = None
        if not showOutput:
            p = subprocess.Popen(args, stderr = subprocess.PIPE, stdout = subprocess.PIPE)
        else:
            p = subprocess.Popen(args)
        return p


class MySQLAdmin:
    def __init__(self, home, pid, conf, host, port, user, psw):
        self.home = home
        self.pid = pid
        self.host = host
        self.port = port
        self.user = user
        self.psw = psw
        self.conf = conf

    def stop_server(self):
        if platform.system() == 'Linux':
            args = [self.home + '/bin/mysqladmin', '-h', self.host, '-P', str(self.port), \
                    '-u', self.user]
            if self.psw != None and self.psw != '':
                args.append('-p')
                args.append(str(self.psw))
            args.append('shutdown')
            print "shutdown mysql server..."
            p = Util.exeCmd(args, showCmd = False, showOutput = True)
            p.wait()
            count = 0
            while os.path.isfile(self.pid):
                time.sleep(2)
                count = count + 1
                if count % 30 == 0:
                    sys.stderr.write("[Warning] It costs lots of time to shutdown mysqld, please " \
                          "check that mysqld is still alive, " \
                          "or pid file '%s' is correct!\n" % self.pid)
            print "mysql has been down!"
        else:
            sys.stderr.write("[WARNING] Can't restart mysql on non-linux platform.\n")

    def start_server(self):
        if platform.system() == 'Linux':
            print "start mysql server..."
            args = [self.home + '/bin/mysqld_safe', '--defaults-file=' + self.conf]
            Util.exeCmd(args, showCmd = False, showOutput = True)
            while not os.path.isfile(self.pid):
                time.sleep(2)
            print "mysql has been started!"
        else:
            sys.stderr.write("[WARNING] Can't restart mysql on non-linux platform.\n")

    def restart_server(self):
        if platform.system() == 'Linux':
            self.stop_server()
            self.start_server()
        else:
            sys.stderr.write("[WARNING] Can't restart mysql on non-linux platform.\n")

class Tester(object):
    _inst = None
    _exit = False

    def __new__(cls):
        if  cls._inst is None:
            cls._inst = object.__new__(cls)
        return  cls._inst

    def __init__(self):
        self.config = None
        self.javaCmd = ['java']

        self.dbOpt = []
        self.commonOpt = []
        self.dbType = None
        self.tableSize = 0
        self.duration = 0
        self.reportDir = "./report"
        self.testCase = None
        self.mysqlAdmin = None
        self.autoRestart = False
        self.testCase = None
        self.threads = 100
        self.engines = None
        self.operations = None
        self.javaInstance = None

    def __getConfig(self, segment, key, necessary = True):
        '''
        get configure item and trim \' in the item
        '''
        if necessary:
            return self.__doGetConfig(segment, key)
        else:
            try:
                return self.__doGetConfig(segment, key)
            except ConfigParser.NoOptionError:
                return None

    def __doGetConfig(self, segment, key):
        v = self.config.get(segment, key)
        v = v.replace('\'', '', 2)
        return v

    def __getTestCaseSetting(self, testCase):
        '''
        get test case settings
        '''
        dic = {'list-blogs':0, 'show-blog':0, 'update-access':0, 'update-comment':0, \
               'show-siblings':0, 'update-blog':0, 'publish-blog':0}
        if (not dic.has_key(testCase)) and testCase != 'mix-tran':
            errMsg = "Unknow test case name:%s\nThe valid test case names you can choose is: \n%s" % \
                     (testCase, str(['mix-tran'] + dic.keys()))
            raise Exception, errMsg
        else:
            dic[testCase] = 1
        args = []
        if testCase != 'mix-tran':
            for key in dic:
                self.__appendOpts(args, '--' + key, str(dic[key]))
        return args

    def __appendOpts(self, args, opt, optValue = None):
        '''
        append an option to the argument list
        '''
        args.append(opt)
        if optValue != None:
            args.append(optValue)
        return args

    def __parseOpts(self, configFileName):
        '''
        parse options in configure file
        '''
        self.config = ConfigParser.ConfigParser()
        cfgfile = open(configFileName,'r')
        try:
            self.config.readfp(cfgfile)

            #parse enviroment options
            javaClassPath = self.__getConfig('enviroment', 'java_classpath')
            if platform.system() == 'Windows':
                javaClassPath = javaClassPath.replace(':', ';')
            mainClass = self.__getConfig('enviroment', 'main_class')
            self.__appendOpts(self.javaCmd, "-classpath", javaClassPath)
            self.__appendOpts(self.javaCmd, mainClass)

            #parse database options
            self.dbType = self.config.get('database', 'db_type')
            host = self.__getConfig('database', 'db_host')
            port = self.__getConfig('database', 'db_port')
            user = self.__getConfig('database', 'db_user')
            psw = self.__getConfig('database', 'db_psw')
            schema = self.__getConfig('database', 'schema_name')

            #parse mysql options
            if self.dbType.upper() == "MYSQL":
                engine = self.config.get('mysql', 'table_engine')
                self.engines = engine.split(',')
                restartStr = self.config.get('mysql', 'auto_restart', False)
                if restartStr != None and restartStr.upper() == 'TRUE':
                    self.autoRestart = True
                mysqlHome = self.config.get('mysql', 'mysql_home')
                mysqlPid = self.config.get('mysql', 'mysql_pid')
                mysqlConf = self.config.get('mysql', 'mysql_config')
                self.mysqlAdmin = MySQLAdmin(mysqlHome, mysqlPid, mysqlConf, host, port, user, psw)

            #build database options
            self.__appendOpts(self.dbOpt, '--database-type', self.dbType)
            self.__appendOpts(self.dbOpt, '--database', schema)
            self.__appendOpts(self.dbOpt, '-h', host)
            self.__appendOpts(self.dbOpt, '-P', port)
            self.__appendOpts(self.dbOpt, '-u', user)
            if psw != None and psw != '':
                self.__appendOpts(self.dbOpt, '-p', psw)

            #parse and build common options
            operStr = self.__getConfig('common', 'operations')
            self.operations = operStr.split(',')
            self.testCase = self.__getConfig('common', 'test_case')
            self.tableSize = self.__getConfig('common', 'table_size')
            self.duration = self.__getConfig('common', 'duration')
            self.threads = self.__getConfig('common', 'threads')
            useTwoTables = self.__getConfig('common', 'use_two_tables', False)
            self.__appendOpts(self.commonOpt, '--table-size', self.tableSize)
            self.__appendOpts(self.commonOpt, '--max-time', self.duration)
            self.__appendOpts(self.commonOpt, '--threads', self.threads)
            if useTwoTables != None:
                self.__appendOpts(self.commonOpt, '--use-two-tables', useTwoTables)
        finally:
            cfgfile.close()

    def __parseNtseOpt(self, args):
        ''''
        parse special options of NTSE storage engine
        '''
        useMms = self.config.get('ntse', 'use_mms', False)
        if useMms != None and useMms.upper() == "FALSE":
            sys.stdout.write("[NOTE] You have specified not to use MMS in NTSE storage engine!\n")
            self.__appendOpts(args, "--ntse-create-table-args", "usemms:false")

    def __doOneTest(self, engine = None, operations = None):
        '''
        do blogbench test once
        '''
        args = self.javaCmd + self.dbOpt + self.commonOpt

        testCaseDir = None
        reportFullPath = None
        if engine != None:
            args.append('--table-engine')
            args.append(engine)
            #parse ntse options
            if engine.upper() == "NTSE":
                self.__parseNtseOpt(args)
            testCaseDir = self.testCase + '-' + engine + '-' + self.tableSize
        else:
            testCaseDir = self.testCase + '-' + self.dbType + '-' + self.tableSize

        reportFullPath = self.reportDir + '/' + testCaseDir
        self.__appendOpts(args, '--report-dir', reportFullPath)

        if operations != None:
            print "Operations %s are specified through command line, so operations %s in configure " \
                "file are skipped." % (operations, self.operations)
            self.operations = operations

        for oper in self.operations:
            if self.autoRestart:
                self.mysqlAdmin.restart_server()
            oper = oper.upper()
            if oper == "LOAD" or oper == "L":
                cmd = copy.deepcopy(args)
                cmd.append("LOAD")
                print "\npreparing blogbench test"
                print "--------------------------------"
                self.javaInstance = Util.exeCmd(cmd)
            elif oper == "RUN" or oper == "R":
                cmd = copy.deepcopy(args) + self.__getTestCaseSetting(self.testCase)
                cmd.append("RUN")
                print "\nrunning blogbench test"
                print "--------------------------------"
                self.javaInstance = Util.exeCmd(cmd)
            else:
                raise Exception, "Invalid operation type: %s" % oper

            while not Tester._exit and None == self.javaInstance.poll():
                time.sleep(1)
            self.javaInstance.wait()

        #compress test result
        if os.path.exists(reportFullPath):
            zipFileName = testCaseDir + '.zip'
            cwd = os.getcwd()
            os.chdir(self.reportDir)
            Util.zipfolder(testCaseDir, zipFileName)
            os.chdir(cwd)
            
    def __setupEnv(self):
    	if platform.system() == 'Windows':
    		os.system("chmod +x *.sh")
			os.system("chmod +x scripts/*")
			os.system("chmod +x scripts/statistician/*")

    def run(self, configFileName, oper):
        '''
        run blogbench test
        '''
        self.__setupEnv()
        signal.signal(signal.SIGINT, Tester.signalHandler)
        self.__parseOpts(configFileName)
        if self.dbType.upper() == "MYSQL":
            for eng in self.engines:
                self.__doOneTest(eng, operations = oper)
        else:
            self.__doOneTest(operations = oper)

    @staticmethod
    def signalHandler(signum, frame):
        print ""
        Tester._exit = True

def main():
    parser = OptionParser()
    parser.set_usage(parser.get_usage().rstrip())
    parser.add_option("-l", "-L", "--load", action="append_const", const="LOAD", dest="oper", help = "load data")
    parser.add_option("-r", "-R", "--run", action="append_const", const="RUN", dest="oper", help = "run test")
    parser.add_option("-c", "--configure-file", dest="configFile", type="string", default='run_blogbench.cfg'
        , help = "path of configure file, default is 'run_blogbench.cfg'")
    (options, args) = parser.parse_args()

    testWorker = Tester()
    testWorker.run(options.configFile, options.oper)

if __name__ == "__main__":
    main()
