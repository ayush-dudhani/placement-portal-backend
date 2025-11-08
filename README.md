## PlacementPortalApplication
### SETUP Prerequisite
- JDK 17
- Java IDE - (Intellij IDEA Preferred)
### BUILD PROJECT
- BUILD command
`mvn clean install`
### RUN PROJECT
- RUN the `PlacementPortalApplication.java` file to run the project


## Contribution Guidelines
### Branching conventions
- Create a branch named feature/<your_name_initials>/<feature-description> (eg. - feature/AD/signin-widget)and then create a pull request describing the changes
- For single file change you can use auto created patch names

### Github CI/CD
- On every new PR and new commit on PR, 2 actions are configured:
- 1 `maven build`
- 2 `Git Guardian Security Check`

// test comment
