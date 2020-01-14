void setBuildStatus(String message, String state) {
    step([
            $class            : "GitHubCommitStatusSetter",
            reposSource       : [$class: "ManuallyEnteredRepositorySource", url: "https://github.com/tardisone/shamu-hrs-company-service.git"],
            contextSource     : [$class: "ManuallyEnteredCommitContextSource", context: "shamu-company-master-build"],
            errorHandlers     : [[$class: "ChangingBuildStatusErrorHandler", result: "UNSTABLE"]],
            statusResultSource: [$class: "ConditionalStatusResultSource", results: [[$class: "AnyBuildResult", message: message, state: state]]]
    ])
}

pipeline {
    agent any
    environment {
        sonarqubeScannerHome = tool name: 'Shamu Hrs Company Service Sonarqube Scanner'
        RELEASE = ''
    }
    stages {
        stage('unit tests') {
            steps {
                echo '---------------------------------\n' +
                        '            Unit test            ' +
                        '\n---------------------------------'

                sh "sudo mvn clean test -Dspring.profiles.active=test > junit_output.txt"
            }
        }
        stage('sonarqube analysis') {
            steps {
                echo '---------------------------------\n' +
                        '      SonarQube analysis         ' +
                        '\n---------------------------------'
                withSonarQubeEnv('ShamuHrsCompanyServiceSonarQube') {
                    sh "${sonarqubeScannerHome}/bin/sonar-scanner"
                }
            }
        }
        stage('create artifact') {
            steps {
                echo '---------------------------------\n' +
                        '      Create artifact         ' +
                        '\n---------------------------------'
                script { RELEASE = sh(returnStdout: true, script: ''' echo ${ref} | sed 's/^.*\\///' ''') }
                sh 'sudo bin/build -e ${params.DEV_ENV} -e ${params.QA_ENV} -r ${RELEASE}'
            }
        }
        stage('deploy master to dev environment') {
            when {
                expression { RELEASE = 'master' }
            }
            steps {
                sh "git checkout origin/master && sudo bin/deploy ${params.DEV_ENV} ${RELEASE}"
            }
        }
    }
    post {
        success {
            setBuildStatus("Build succeeded", "SUCCESS")
        }
        failure {
            setBuildStatus("Build failed", "FAILURE")
        }
    }
}
