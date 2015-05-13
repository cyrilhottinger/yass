cd $(dirname $0)
read -p "Version [ MAJOR.MINOR.PATCH or 'enter' for <none> ]?: " version
version=${version:-0.0.0}
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.7.0_80.jdk/Contents/Home
~/development/gradle-2.3/bin/gradle -Pversion=$version
