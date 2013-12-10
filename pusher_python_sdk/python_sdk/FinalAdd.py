#!/usr/bin/python
# _*_ coding: UTF-8 _*_

import sys
reload(sys) 
sys.setdefaultencoding('utf-8')
import time
import urllib2  
import re  
import pymongo
import time
import sched,os,threading
from xml.etree.ElementTree import ElementTree,Element

def prettyXml(element, indent, newline, level = 0): # elemnt为传进来的Elment类，参数indent用于缩进，newline用于换行    
    if element:  # 判断element是否有子元素    
        if element.text == None or element.text.isspace(): # 如果element的text没有内容    
            element.text = newline + indent * (level + 1)      
        else:    
            element.text = newline + indent * (level + 1) + element.text.strip() + newline + indent * (level + 1)    
    #else:  # 此处两行如果把注释去掉，Element的text也会另起一行    
        #element.text = newline + indent * (level + 1) + element.text.strip() + newline + indent * level    
    temp = list(element) # 将elemnt转成list    
    for subelement in temp:    
        if temp.index(subelement) < (len(temp) - 1): # 如果不是list的最后一个元素，说明下一个行是同级别元素的起始，缩进应一致    
            subelement.tail = newline + indent * (level + 1)    
        else:  # 如果是list的最后一个元素， 说明下一行是母元素的结束，缩进应该少一个    
            subelement.tail = newline + indent * level    
        prettyXml(subelement, indent, newline, level = level + 1) # 对子元素进行递归操作

def read_xml(in_path):  
    '''''读取并解析xml文件 
       in_path: xml路径 
       return: ElementTree'''  
    tree = ElementTree()  
    tree.parse(in_path)  
    return tree  

def write_xml(tree, out_path):  
    '''''将xml文件写出 
       tree: xml树 
       out_path: 写出路径'''  
    tree.write(out_path, encoding="utf-8",xml_declaration=True)

def add_child_node(nodelist, element):  
    nodelist.append(element)  

def create_node(tag, property_map, content):  
    '''''新造一个节点 
       tag:节点标签 
       property_map:属性及属性值map 
       content: 节点闭合标签里的文本内容 
       return 新节点'''  
    element = Element(tag, property_map)  
    element.text = content  
    return element 

url = 'http://tieba.baidu.com/p/2744503599'

s = sched.scheduler(time.time,time.sleep)

def performDedail(urlll = url):
	find_re = re.compile(r'<h1 class="core_title_txt" title="(.+?)">.+?近二十章连载(.+?)未完待续.+?<div class="share_btn_wrapper">'.encode('gb2312'), re.DOTALL)  
	html = urllib2.urlopen(urlll).read()  
	for x in find_re.findall(html):  
		str1 = x[0].decode('gb2312', 'ignore').encode('utf-8')
		str2 = x[1].decode('gb2312', 'ignore').encode('utf-8')
		str2 = re.sub(r'(http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\w\-\.,@?^=%&amp;:/~\+#]*[\w\-\@?^=%&amp;/~\+#])?','',str2,0)
		str2 = str2.replace('><a href=""','').replace('target="_blank"','').replace('</a>','').replace('-','')
		num = re.search(r'[^：\s?br<> ]',str2).start()
		str2 = str2[num:len(str2)] + '未完待续)'
#		print str1, str2
		#1. 读取xml文件  
		tree = read_xml("./test.xml")		
		#A. 找到父节点  
		nodes = tree.getroot()
		name = len(nodes.findall('chapter')) + 1
		#A.新建节点  
		a = create_node("chapter", {"file":str(name).zfill(5),"name":str1},str(time.strftime('%Y-%m-%d %H:%M:%S', time.localtime())))  
		#B.插入到父节点之下  
		add_child_node(nodes, a)
		prettyXml(nodes, '\t', '\n')
		write_xml(tree, "./test.xml")
		f = open(os.getcwd() + '/xiaoshuo/' + str(name).zfill(5) + '.txt','w')
		f.write(str2)
		f.close()
	print 'Done!',"Current Time:", time.strftime("%Y-%m-%d %A %X %Z", time.localtime())

def perthread(inc):
	print 'count',threading.activeCount()
	s.enter(inc,0,perthread,(inc,))
	t1 = threading.Thread(target=performDedail)
	t1.setDaemon(True)
	t1.start()
	t1.join(15)

def mymain(inc=20):
   s.enter(0,0,perthread,(inc,))
   s.run()

if __name__ == "__main__":
    mymain() 
