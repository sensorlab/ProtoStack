ProtoStack
==========

ProtoStack is a tool for experimentation with dynamic composition of communication services.
The implementation of ProtoStack was triggered by a wireless sensor network testbed
and is used for experimentation with cognitive radio and cognitive networking in the
frame of the CREW project. As such, ProtoStack is designed in a way to
ease research and experimentation with communication networks, particularly with
cognitive networks. The system was designed so that an advanced user, such as the
component developer needs, to focus on developing the component and make it work
with Contiki OS and a novice user needs only to focus on composing services in a stack
using the workbench.

ProtoStack needs the following components:
- JRE6 or newer
- Apache
- Tomcat
- Sesame 2.x (http://www.openrdf.org/download.jsp)
- The WireIT library
- Contiki OS
- The toolchain allowing building the Contiki image and programming your HW with it

ProtoStack's components are:
- The server (https://github.com/sensorlab/ProtoStack/tree/master/src)
- The ontology (https://github.com/sensorlab/ProtoStack/tree/master/owl)
- The workbench (https://github.com/sensorlab/ProtoStack/tree/master/crimeLayers)
- The CRime library (https://github.com/sensorlab/CRime)


More detailed documentation will come soon.
