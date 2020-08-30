FROM gradle:6.4-jdk11 AS build
COPY ./ .
RUN gradle clean dockerPrepare

FROM adoptopenjdk/openjdk12:jdk-12.0.2_10-slim
#ENV 

# Google Chrome

RUN apt-get update \
    && apt-get install wget \
    && apt-get install -y gnupg \
    && apt-get install -y unzip


ARG CHROME_VERSION=85.0.4183.83-1
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
	&& echo "deb http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google-chrome.list \
	&& apt-get update -qqy \
	&& apt-get -qqy install google-chrome-stable=$CHROME_VERSION \
	&& rm /etc/apt/sources.list.d/google-chrome.list \
	&& rm -rf /var/lib/apt/lists/* /var/cache/apt/* \
	&& sed -i 's/"$HERE\/chrome"/"$HERE\/chrome" --no-sandbox/g' /opt/google/chrome/google-chrome

# ChromeDriver

ARG CHROME_DRIVER_VERSION=85.0.4183.87
RUN wget -q -O /tmp/chromedriver.zip https://chromedriver.storage.googleapis.com/$CHROME_DRIVER_VERSION/chromedriver_linux64.zip \
	&& unzip /tmp/chromedriver.zip -d /opt \
	&& rm /tmp/chromedriver.zip \
	&& mv /opt/chromedriver /opt/chromedriver-$CHROME_DRIVER_VERSION \
	&& chmod 755 /opt/chromedriver-$CHROME_DRIVER_VERSION \
	&& ln -s /opt/chromedriver-$CHROME_DRIVER_VERSION /usr/bin/chromedriver

WORKDIR /home
COPY --from=build /home/gradle/build/docker /home/gradle/formParser.properties /home/gradle/log4j.properties /home/gradle/config.ini ./
ENTRYPOINT ["/home/remotehand/bin/remotehand", "-httpserver", "-enableEnvVars"]