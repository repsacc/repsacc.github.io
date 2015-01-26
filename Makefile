JAVAC     = javac
JAVAFLAGS = -classpath "../lib/*:."
	SOURCES   = $(wildcard *.java)
	CLASSES   = $(SOURCES:.java=.class)

%.class: %.java
	$(JAVAC) $(JAVAFLAGS) $<

all: $(CLASSES)

clean:
	rm $(CLASSES)

