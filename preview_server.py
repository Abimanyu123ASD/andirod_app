
#!/usr/bin/env python3
import http.server
import socketserver
import os

class MyHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/' or self.path == '/index.html':
            self.send_response(200)
            self.send_header('Content-type', 'text/html')
            self.end_headers()
            
            html_content = '''
            <!DOCTYPE html>
            <html>
            <head>
                <title>CBT Android App Preview</title>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { 
                        font-family: Arial, sans-serif; 
                        margin: 40px; 
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        min-height: 100vh;
                    }
                    .container { 
                        max-width: 800px; 
                        margin: 0 auto; 
                        background: rgba(255,255,255,0.1);
                        padding: 30px;
                        border-radius: 15px;
                        backdrop-filter: blur(10px);
                    }
                    h1 { color: #fff; text-align: center; margin-bottom: 30px; }
                    .feature { 
                        background: rgba(255,255,255,0.1); 
                        padding: 20px; 
                        margin: 15px 0; 
                        border-radius: 10px;
                        border-left: 4px solid #4CAF50;
                    }
                    .code { 
                        background: rgba(0,0,0,0.3); 
                        padding: 15px; 
                        border-radius: 5px; 
                        font-family: monospace;
                        overflow-x: auto;
                    }
                    .button {
                        background: #4CAF50;
                        color: white;
                        padding: 12px 24px;
                        border: none;
                        border-radius: 5px;
                        cursor: pointer;
                        font-size: 16px;
                        margin: 10px;
                        text-decoration: none;
                        display: inline-block;
                    }
                    .button:hover { background: #45a049; }
                    .status { 
                        text-align: center; 
                        padding: 20px; 
                        background: rgba(255,255,255,0.1);
                        border-radius: 10px;
                        margin: 20px 0;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🤖 CBT Android App Preview</h1>
                    
                    <div class="status">
                        <h2>📱 Android WebView App</h2>
                        <p>This is a hybrid Android app that loads a web application in a WebView component.</p>
                    </div>

                    <div class="feature">
                        <h3>🌐 App URL</h3>
                        <div class="code">https://ct-uat.infinitisoftware.net/</div>
                        <p>The app loads this URL in a full-screen WebView with enhanced functionality.</p>
                    </div>

                    <div class="feature">
                        <h3>⬇️ File Download Support</h3>
                        <p>• Intercepts blob downloads from web content</p>
                        <p>• Saves files to device Downloads folder</p>
                        <p>• Supports PDF, CSV, Excel files</p>
                        <p>• Uses Android MediaStore API for modern devices</p>
                    </div>

                    <div class="feature">
                        <h3>🔧 Technical Features</h3>
                        <p>• JavaScript enabled WebView</p>
                        <p>• Cookie support for sessions</p>
                        <p>• Storage permissions handling</p>
                        <p>• Network connectivity checking</p>
                        <p>• Error handling with custom error activity</p>
                    </div>

                    <div class="feature">
                        <h3>📋 App Structure</h3>
                        <div class="code">
MainActivity.java - Main WebView activity<br>
ErrorActivity.java - Network error handling<br>
CommonFunctions.java - Shared utilities<br>
                        </div>
                    </div>

                    <div class="feature">
                        <h3>🛠️ Build Status</h3>
                        <p>To build the Android APK, Java environment needs to be configured.</p>
                        <a href="#" onclick="buildApp()" class="button">Attempt Build</a>
                        <div id="build-output" style="margin-top: 15px;"></div>
                    </div>
                </div>

                <script>
                    function buildApp() {
                        document.getElementById('build-output').innerHTML = 
                            '<div style="background: rgba(255,255,255,0.1); padding: 15px; border-radius: 5px;">' +
                            '⚠️ Android apps require Android Studio or gradle with Android SDK to build.<br>' +
                            'This preview shows the app structure and functionality.<br>' +
                            'To test the actual app, you would need to build and install the APK on an Android device.' +
                            '</div>';
                    }
                </script>
            </body>
            </html>
            '''
            self.wfile.write(html_content.encode())
        else:
            super().do_GET()

PORT = 5000
Handler = MyHTTPRequestHandler

with socketserver.TCPServer(("0.0.0.0", PORT), Handler) as httpd:
    print(f"Preview server running at http://0.0.0.0:{PORT}")
    print("This shows information about your Android app")
    httpd.serve_forever()
