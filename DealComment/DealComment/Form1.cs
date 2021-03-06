﻿using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Windows.Forms;

namespace DealComment
{
    public partial class Form1 : Form
    {
        public Form1()
        {
            InitializeComponent();
        }

        private void btnConvert_Click(object sender, EventArgs e)
        {
            try
            {
                string strPath = this.txtFile.Text;
                if (File.Exists(strPath))
                {
                    //单行注释匹配
                    const string strPatternCommentLine = @"\/\/.*";
                    //多行注释匹配
                    const string strPatternCommentBlock = @"\/\*(.|[\r\n])*?\*/";
                    //空行匹配
                    const string strPatternBlank = @"\n[\s| ]*\r";
                    //去除region块
                    const string strRegionPattern = @"#[(region)|(endregion) ]{1,1}.*";

                    var strNewFilePath = Path.Combine(Path.GetDirectoryName(strPath), "new" + Path.GetFileName(strPath));

                    var strFileContent = File.ReadAllText(strPath, Encoding.UTF8);
                    //去除单行注释
                    strFileContent = Regex.Replace(strFileContent, strPatternCommentLine, "\r", RegexOptions.IgnoreCase);
                    //去除多行注释
                    strFileContent = Regex.Replace(strFileContent, strPatternCommentBlock, "\r", RegexOptions.IgnoreCase);
                    //去除region块
                    strFileContent = Regex.Replace(strFileContent, strRegionPattern, "\r", RegexOptions.IgnoreCase);
                    //去空行
                    strFileContent = Regex.Replace(strFileContent, strPatternBlank, "", RegexOptions.IgnoreCase).Trim();
                    File.WriteAllText(strNewFilePath, strFileContent, Encoding.UTF8);

                    MessageBox.Show(@"Well Done!");
                }
                else
                {
                    MessageBox.Show(@"File not correct!");
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(@"System error:" + ex.Message);
            }
        }
        
        private void Form1_DragDrop(object sender, DragEventArgs e)
        {
            this.txtFile.Text = ((System.Array)e.Data.GetData(DataFormats.FileDrop)).GetValue(0).ToString();
        }

        private void Form1_DragEnter(object sender, DragEventArgs e)
        {
            if (e.Data.GetDataPresent(DataFormats.FileDrop))
                e.Effect = DragDropEffects.Link;
            else
                e.Effect = DragDropEffects.None;
        }
    }
}
