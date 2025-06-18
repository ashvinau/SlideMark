![] (headersmall.png)
->
###### Presentations made easy with SlideMark!
Presenters: _Joe Wigdor_ and _Tamara Slone_
**Capstone Presentation 2025**
=== double-vertical
## Objectives

=== single-left

##### What is SlideMark?
- SlideMark is a markdown-like parser and renderer for presentation slides.

- A dual-pane GUI editor allows the user to create a presentation slide deck using a version of markdown which includes functionality to demarcate slides and describe slide styling and layout.

- Initially this application will be offline and cross-platform, with the potential for a web app in the future. 
->
##### Our Purpose
- This software aims to provide an alternative to traditional point-and-click presentation tools like Microsoft Powerpoint or LibreOffice Impress. 
- The idea is to reduce a slide to its component parts via HTML/CSS templates, then fill those components in with text and images formatted with markdown. 

- Ideally, a user would be able to prepare a slide deck by writing source in markdown rather than fiddling with handles and spinners in a UI.

=== double-horizontal
#### Our System Design 
##### Mediator Pattern
---
![] (sysarch.png)
->

#### Challenges
- Establishing a messaging system
- Directing requests to modules
- Avoiding direct composition
- Debugging
->

#### Benefits
- Preventing tight coupling
- Reusability
- Modularity
- Stability

=== single-double-vertical


##### Renderer Scope
- Contains the Slide Panel
- Presents the HTML passed from Parser
- WebView displays the slide and design
- Presentation Mode
- Slide functionality
->

##### Parser Scope
- Interprets standard markdown tokens
- Additional tokens for slide formatting
- Generates a list of raw tags
- Provides data in a custom range of slides
->
##### Editor Scope
- Ability to edit text with line numbers
- Saving and loading of markdown files
- Keeping track of:
	- The active file
	- The working directory
->
##### GUI Scope
- The entire User Interface 
- The "container" for Renderer and Editor 
- Contains the MenuBar and Toolbar functions

=== two-by-two
## Original UI
![] (originalui.png)

=== single-center
## Final UI
![] (finalUI.png)

=== single-center
![] (alltemplates.png)
->
## Templates
- SlideMark provides pre-made templates that allow the user to choose how to present a slide.
- Templates are designed in HTML and CSS for wider customization.
- Future iterations would allow the user to implement their own designs.

=== double-horizontal

# Growth

=== single-right
### Joe
Development allowed me to improve:
- Software modeling
- Pattern implementation
- String manipulation and replacement
- Regular Expression pattern matching
- State machine implementation
- GUI programming with JavaFX

->
### Tamara

Creating SlideMark has helped me:
- Strengthen my Java skills
- Given me experience with GUI programming in JavaFX
- How to develop with a mediator pattern
- Expanded my UX/UI Design skills
- Experience with UX Development
=== double-horizontal

# Impact
=== single-left
## Global Impact
SlideMark will make a new alternative to traditional, mouse-driven presentation software available. Technically inclined users generally prefer markup for document formatting; we aim to provide this for presentations.

->
#### Tamara
- Provided UX Development skills for my portfolio
- Experience of contributing to a future open source project
- Helped me go out of my comfort zone from web development to a GUI application.
->
#### Joe 
- Created an application that I find useful
- Deepened my understanding of parser development
- Great collaboration experience
- Looking forward to seeing what happens!
=== single-double-vertical

# Demo Time!
=== single-center
## Download SlideMark Today!

->
![] (Header.png)

Alpha 0.1 Available **Now!**
[SlideMark @ GitHub] (https://github.com/ashvinau/SlideMark)
*https://github.com/ashvinau/SlideMark*


=== double-vertical
# Any Questions?
###### **Email us:** slidemark@scholomance.io
=== single-left
