cd $(dirname $0)
read -p "Version [ MAJOR.MINOR.PATCH or 'enter' for <none> ]?: " version
version=${version:-0.0.0}
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home
~/development/gradle-2.4/bin/gradle -Pversion=$version
