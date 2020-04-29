ARG APP_INSIGHTS_AGENT_VERSION=2.5.1
FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.4

# Mandatory!
ENV APP finrem-evidence-management-client-api.jar

COPY build/libs/$APP /opt/app/
COPY lib/AI-Agent.xml /opt/app/

EXPOSE 4006

CMD ["finrem-evidence-management-client-api.jar"]
