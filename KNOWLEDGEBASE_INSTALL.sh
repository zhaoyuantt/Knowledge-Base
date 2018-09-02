#!/bin/bash
#AUTO DEPLOY KNOWLEDGE BASE SYSTEM
#BY AUTHORS ZHAOYUAN 2018-08-31 

echo "****************INSTALL JDK7u80***************"
JAVA_VERSION=`java -version 2>&1 |awk 'NR==1{ gsub(/"/,""); print $3 }'`
echo "系统当前JDK版本："$JAVA_VERSION
VERSION="7"
#result=$(echo $JAVA_VERSION | grep "${VERSION}")
	if [[ $JAVA_VERSION =~ $VERSION ]];then
		echo "****************THE SYSTEM CHECK THAT THE CURRENT JDK VERSION 1.7.0_80 WITHOUT INSTALLATION****************"
	else
		 WORKDIR="/lfile"
		 JDKFILE="jdk-7u80-linux-x64.tar.gz"
		 if [ ! -d $WORKDIR ];then
			    echo "***********不满足本脚本执行的要求****************"
    			exit 0
		 fi
         if [ ! -f $JDKFILE ];then
                echo "***********jdk-7u80-linux-x64.tar.gz DOES NOT EXIST*************"
                exit 0
		 fi
		 mkdir /usr/local/java
		 tar  -zxvf  jdk-7u80-linux-x64.tar.gz -C /usr/local/java
		 echo "****************jdk-7u80-linux-x64.tar.gz UNZIP THE SUCCESSED******************"
		 DHOME="JAVA_HOME"
             echo "export JAVA_HOME=/usr/local/java/jdk1.7.0_80">>/etc/profile
		 echo "export JRE_HOME=$"$DHOME"/jre">>/etc/profile
		 RHOME="JRE_HOME"
		 echo "export CLASSPATH=.:$"$DHOME"/lib:$"$RHOME"/lib">>/etc/profile
		 PPATH="PATH"
		 echo "export PATH=$"$DHOME"/bin:$"$PPATH>>/etc/profile
		 source /etc/profile
		 echo "***********jdk-7u80-linux-x64.tar.gz INSTALL SUCCESSED*************"
	fi

echo "**************INSTALL LIBREOFFICE************"
LIBREOFFICE="LibreOffice_5.4.7_Linux_x86-64_rpm.tar.gz"

      if [ ! -f $LIBREOFFICE  ];then
	        echo "***********LibreOffice_5.4.7_Linux_x86-64_rpm.tar.gz DOES NOT EXIST*************"
            exit 0
      else
			UNZIPLIBREOFFICEDIR="/usr/local/LibreOffice"
			if [ -d $UNZIPLIBREOFFICEDIR ];then
				 echo "****************LibreOffice_5.4.7_Linux_x86-64_rpm.tar.gz ALREADY UNZIP************************"
			else
				 mkdir /usr/local/LibreOffice
				 tar  -zxvf  LibreOffice_5.4.7_Linux_x86-64_rpm.tar.gz -C /usr/local/LibreOffice
				 echo "****************LibreOffice_5.4.7_Linux_x86-64_rpm.tar.gz UNZIP THE SUCCESSED******************"
			       cd /usr/local/LibreOffice/LibreOffice_5.4.7.2_Linux_x86-64_rpm/RPMS  && rpm  -ivh  --nodeps  lib*.rpm
				 echo "****************LibreOffice_5.4.7 SAARTING******************"
				 /opt/libreoffice5.4/program/soffice -headless -accept="socket,host=127.0.0.1,port=8100;urp;" -nofirststartwizard &
			fi
      fi

echo "****************INSTALL APACHE TOMCAT****************"
APACHETOMCATFILE="apache-tomcat-7.0.52.tar.gz"
WCPFILE="wcp.war"
WDAFILE="wda.war"

	if [ ! -f $APACHETOMCATFILE ] || [ ! -f $WCPFILE ] || [ ! -f $WDAFILE ];then
		echo "***********apache-tomcat-7.0.52.tar.gz OR wcp.war Or wda.war DOES NOT EXIST*************"
		exit 0
	else
		UNZIPAPACHETOMCATDIR="/ApacheTomcat"
		if [  -d $UNZIPAPACHETOMCATDIR ];then
			 echo "****************apache-tomcat-7.0.52.tar.gz ALREADY UNZIP************************" 
		else
			 mkdir /ApacheTomcat
			 tar -zxvf apache-tomcat-7.0.52.tar.gz -C /ApacheTomcat
			 echo "****************apache-tomcat-7.0.52.tar.gz UNZIP THE SUCCESSED******************"
                    cp wcp.war /ApacheTomcat/apache-tomcat-7.0.52/webapps && cp wda.war /ApacheTomcat/apache-tomcat-7.0.52/webapps
 			 cd /ApacheTomcat/apache-tomcat-7.0.52/bin && ./startup.sh && tail -f ../logs/catalina.out
             echo "****************DEPLOY FINISH,PLEASE GO TO MODIFY THE WCP AND WDA PROJECT .PROPERTIES FILE ******************"
			 echo "****************DIRECTORY AT ApacheTomcat/apache-tomcat-7.0.52/webapps/wcp/WEB-INF/classes ******************" 
		fi
	fi


