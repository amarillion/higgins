#!groovy

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

		// stage('Package') {
			// 'amake/innosetup' has this weird thing that it runs innosetup directly,
			// and you need to pass only the name of the iss file.
			// Docker-pipeline is no help here, 
			// only way to make it work is to call docker 'manually'

			// TODO - this almost works! But fails because the can't write to the current working directory because it's owned by jenkins.
			// sh 'docker run --rm -i -v $(pwd):/work amake/innosetup higgins.iss'
			// TODO - this fails because wine is strict about only providing access for one particular user
			// sh 'docker run --rm -i -u $(id -u):$(id -g) -v $(pwd):/work amake/innosetup higgins.iss' 

			// archiveArtifacts artifacts: 'DrHiggins*.exe'
		// }

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
