#!groovy
import groovy.json.JsonSlurper

node {
	
	catchError {

		stage('CheckOut') {

			checkout scm

		}

		stage('Build') {

			echo "Running ${env.BUILD_ID} on ${env.JENKINS_URL}."
			docker.image('cameronmcnz/ant-jdk8-git:latest').inside() {
				sh "ant"
			}
		}

		stage('Package') {

			docker.image('amake/innosetup').inside() {
				sh "higgins.iss"
			}
			
			archiveArtifacts artifacts: 'DrHiggins/*.exe'
		}

	}
	
//	mailIfStatusChanged env.EMAIL_RECIPIENTS
	mailIfStatusChanged "mvaniersel@gmail.com"

}


//see: https://github.com/triologygmbh/jenkinsfile/blob/4b-scripted/Jenkinsfile
def mailIfStatusChanged(String recipients) {
    
	// Also send "back to normal" emails. Mailer seems to check build result, but SUCCESS is not set at this point.
    if (currentBuild.currentResult == 'SUCCESS') {
        currentBuild.result = 'SUCCESS'
    }
    step([$class: 'Mailer', recipients: recipients])
}
