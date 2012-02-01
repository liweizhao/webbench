
__author__="ylh"
__date__ ="$2009-4-14 13:02:17$"

import subprocess
import shlex

class MysqlConfig:
    def __init__(self, home, host, port, user, password):
        self.mysqlHome = home
        self.mysqlHost = host
        self.mysqlPort = port
        self.mysqlUser = user
        self.mysqlPassord = password
        self.cmd = "%(home)s/bin/mysql -h %(host)s -u %(user)s -P %(port)d " % \
             {"home": home, "host": host, "user": user, "port": port}
        if password != "":
            self.cmd = self.cmd + "-p" + password

        print self.cmd

        data = self.executesql("show variables like 'version';")
        self.version = data.splitlines()[1].split()[1]
        data = self.executesql("show variables like 'basedir';")
        self.mysqlInstallPath = data.splitlines()[1].split()[1]

    def executesql(self, sql, outfilename = "", dbname = ""):
        cmd = self.cmd;
        if dbname != "":
            cmd += " -D " + dbname
        cmd += ' -e "' + sql + '"'
        print cmd
        p = subprocess.Popen(shlex.split(cmd),stdin=subprocess.PIPE, stdout=subprocess.PIPE, close_fds=True)
        (r, w) = (p.stdout, p.stdin)
        output = r.read()
        r.close()
        w.close()
        if outfilename != "":
            f = open(outfilename, 'w')
            f.write(output)
            f.close()
        return output;

