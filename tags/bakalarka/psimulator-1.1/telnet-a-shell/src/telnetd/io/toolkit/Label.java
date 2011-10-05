//License
/***
 * Java TelnetD library (embeddable telnet daemon)
 * Copyright (c) 2000-2005 Dieter Wimberger 
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 ***/
package telnetd.io.toolkit;import telnetd.io.BasicTerminalIO;import telnetd.io.terminal.ColorHelper;import java.io.IOException;/** * Class that represents a label. * * @author Dieter Wimberger * @version 2.0 (16/07/2006) */public class Label extends InertComponent {  //Members  private String m_Content;  /**   * Constructs a Label instance.   *   * @param io   Instance of a class implementing the BasicTerminalIO interface.   * @param name String that represents the components name.   * @param text String that represents the visible label.   */  public Label(BasicTerminalIO io, String name, String text) {    super(io, name);    setText(text);  }//constructor  /**   * Constructs a Label instance, using the name as visible content.   *   * @param io   Instance of a class implementing the BasicTerminalIO interface.   * @param name String that represents the components name.   */  public Label(BasicTerminalIO io, String name) {    super(io, name);    setText(name);  }//constructor  /**   * Mutator method for the text property of the label component.   *   * @param text String displayed on the terminal.   */  public void setText(String text) {    //set member    m_Content = text;    //set Dimensions    m_Dim = new Dimension((int) ColorHelper.getVisibleLength(text), 1);  }//setText  /**   * Accessor method for the text property of the label component.   *   * @return String that is displayed when the label is drawn.   */  public String getText() {    return m_Content;  }//getText  /**   * Method that draws the label on the screen.   */  public void draw() throws IOException {    if (m_Position == null) {      m_IO.write(m_Content);    } else {      m_IO.storeCursor();      m_IO.setCursor(m_Position.getRow(), m_Position.getColumn());      m_IO.write(m_Content);      m_IO.restoreCursor();      m_IO.flush();    }  }//draw}//class Label