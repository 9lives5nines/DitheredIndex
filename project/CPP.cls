%--------------------------------------------------------------------
%Created on:    Nov. 03, 2006
%Author:        Tzvetelin Petrov
%--------------------------------------------------------------------

%--------------------------------------------------------------------
% Register the class.
%--------------------------------------------------------------------
    \NeedsTeXFormat{LaTeX2e} \ProvidesClass{CPP}[2006/11/03 Cal Poly Pomona Grad Study Format]

%--------------------------------------------------------------------
% Load the article class to handle unknown options.
%--------------------------------------------------------------------
    \DeclareOption*{\PassOptionsToClass{\CurrentOption}{article}}
    \ProcessOptions\relax \LoadClass[letterpaper,12pt]{article}

%--------------------------------------------------------------------
% Define variables.
%--------------------------------------------------------------------
    %Title name line 1.
    \newcommand{\titleone}[1]{\gdef\@titleone{\mbox{\uppercase{#1}}}}

    %Title name line 2.
    \newcommand{\titletwo}[1]{\gdef\@titletwo{\mbox{\uppercase{#1}}}}
    
    %The type of this document: Thesis/Project.
    \newcommand{\doctype}[1]{\gdef\@doctype{#1}}
    
    %The type of this document in UPPERCASE: Thesis/Project.
    \newcommand{\doctypeUp}[1]{\gdef\@doctypeUp{\mbox{\uppercase{#1}}}}

    %Full name of the degree: Master of Science.
    \newcommand{\degree}[1]{\gdef\@degree{#1}}
    
    %Full name of the field in which the degree is awarded: Mathematics.
    \newcommand{\field}[1]{\gdef\@field{#1}}    

    %Full name of the author.
    \newcommand{\Author}[1]{\gdef\@Author{#1}}
    
    %Full name of the advisor
    \newcommand{\Advisor}[1]{\gdef\@Advisor{#1}}

    %Full name of the committee member 2
    \newcommand{\MemberA}[1]{\gdef\@MemberA{#1}}

    %Full name of the committee member 3
    \newcommand{\MemberB}[1]{\gdef\@MemberB{#1}}    

    %Date the degree is awarded.
    \newcommand{\Year}[1]{\gdef\@Year{#1}}
    \newcommand{\quarter}[1]{\gdef\@quarter{#1}}
    
    %The acknowledgments text
    \newcommand{\Acknowledgments}[1]{\gdef\@Acknowledgments{#1}}
    
    %The abstract text
    \newcommand{\Abstract}[1]{\gdef\@Abstract{#1}}

%--------------------------------------------------------------------
% Line spacing.
%--------------------------------------------------------------------
\newlength{\defbaselineskip}
\setlength{\defbaselineskip}{\baselineskip}
\newcommand{\setlinespacing}[1]%
           {\setlength{\baselineskip}{#1 \defbaselineskip}}
\newcommand{\doublespaceing}{\setlength{\baselineskip}%
                           {2 \defbaselineskip}}
\newcommand{\singlespacing}{\setlength{\baselineskip}{\defbaselineskip}}
\setlength\arraycolsep{2pt}

%--------------------------------------------------------------------
% Set the margins.
%--------------------------------------------------------------------
    %Paper size
    \setlength{\paperheight}{11in}
    \setlength{\paperwidth}{8.5in}
    
    %Left margin
    \setlength{\marginparwidth}{0in}
    \setlength{\marginparsep}{0in}
    \setlength{\oddsidemargin}{0.5in}
    \setlength{\evensidemargin}{0in}
    
    %Right margin
    \setlength{\textwidth}{5.85in} 
    
    %Top margin
    \setlength{\voffset}{0in}
    \setlength{\topmargin}{0in}

    %Bottom margin
    \setlength{\headheight}{0.125in}
    \setlength{\headsep}{0.25in}
    \setlength{\textheight}{8.375in}
    \setlength{\footskip}{0.50in}
    
    \raggedbottom

%\setlength{\paperheight}{11.5in} 
%\setlength{\headsep}{0in}
%\setlength{\topmargin}{0in} 
%\setlength{\headheight}{0in}
%\setlength{\voffset}{0.5in} 
%\setlength{\oddsidemargin}{0.75in}
%\setlength{\evensidemargin}{0.75in} 
%\setlength{\textheight}{8.0in}
%\setlength{\textwidth}{5.25in} 
%\setlength{\footskip}{.95in}
%\linespread{1.67}
%\setlength{\footskip}{.7in}

%--------------------------------------------------------------------
% Cover Sheet.
%--------------------------------------------------------------------
\newcommand{\coversheet}{
    \newpage
    \null
    \pagestyle{empty}
}
%--------------------------------------------------------------------
% Title Page.
%--------------------------------------------------------------------
\renewcommand{\titlepage}{
    \newpage
    %set page numbering to roman, reset counting from 1, 
    %and do not display on this page
    \pagestyle{plain} 
    \thispagestyle{empty}
    \pagenumbering{roman}
    \begin{center} 
        \vspace*{0.375in}
        {\textbf \@titleone}\\ [0.8\baselineskip]
        {\textbf \@titletwo}\\
        \vskip1in
        {\small A \@doctype}\\[0.6\baselineskip] 
        {\small Presented to the}\\[0.6\baselineskip]
        {\small Faculty of}\\[0.6\baselineskip]
        {\small California State Polytechnic University, Pomona}\\[0.6\baselineskip]
        \vskip1in
        {\small In Partial Fulfillment}\\[0.6\baselineskip]
        {\small Of the Requirements for the Degree}\\[0.6\baselineskip]
        {\small \@degree}\\[0.6\baselineskip]
        {\small In}\\[0.6\baselineskip]
        {\small \@field}\\[0.6\baselineskip]
        \vskip1in
        {\small By}\\[0.6\baselineskip]
        {\small \@Author}\\[0.6\baselineskip]
        {\small \@Year}\\[0.6\baselineskip]
    \end{center}
}


%--------------------------------------------------------------------
% Signature page. Phil's Version
%--------------------------------------------------------------------
\newcommand{\signaturepage}{
    \newpage
    \begin{center}   
        \vspace*{0.375in}
        \uppercase{\textbf{Signature Page}}\\[3\baselineskip]
    \end{center}
    \noindent \textbf{\small \@doctypeUp :}\hspace{1.2in} {\small \@titleone}\\[2.5\baselineskip]
    %\null\hspace{1.74in}{\small \@titletwo}\\[1.2\baselineskip]
    \textbf{\small AUTHOR:}\hspace{1.32in}{\small \@Author}\\[2\baselineskip]
    \textbf{\small DATE SUBMITTED:}\hspace{0.48in}{\small \@quarter~\@Year}\\[0.8\baselineskip]
    \null\hspace{2.09in} Computer Science Department\\[3\baselineskip]
    {\small Dr. \@Advisor}\hspace{\fill}\rule{3.7in}{0.4pt}\\[0.05\baselineskip]
    {\small Thesis Committee Chair}\\[0.05\baselineskip]
    {\small Computer Science}\\[3\baselineskip]
    {\small Dr. \@MemberA}\hspace{\fill}\rule{3.7in}{0.4pt}\\[0.05\baselineskip]
    {\small Computer Science}\\[3\baselineskip]
%    {\small Dr. \@MemberB}\hspace{\fill}\rule{3.7in}{0.4pt}\\[0.05\baselineskip]
%    {\small Computer Science}\\[3\baselineskip]
}


%--------------------------------------------------------------------
% Acknowledgements page.
%--------------------------------------------------------------------
\newcommand{\acknowledgmentspage}{
    \newpage
    \begin{center}    
        %\vspace*{0.375in}
        \uppercase{\textbf{Acknowledgments}}\\[3\baselineskip]
    \end{center}
    \renewcommand{\baselinestretch}{1.66} \small\normalsize%
    \@Acknowledgments     
}
%--------------------------------------------------------------------
% Abstract page.
%--------------------------------------------------------------------
\newcommand{\abstractpage}{
    \newpage
    \begin{center}    
        %\vspace*{0.375in}
        \uppercase{\textbf{Abstract}}\\[3\baselineskip]
    \end{center}
    \renewcommand{\baselinestretch}{1.66} \small\normalsize%
    \@Abstract   
}
