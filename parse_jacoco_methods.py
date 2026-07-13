import xml.etree.ElementTree as ET
import glob

def parse_report(file_path):
    tree = ET.parse(file_path)
    root = tree.getroot()

    for package in root.findall('package'):
        for cls in package.findall('class'):
            class_name = cls.get('name')
            if 'GlobalEdgeSet' in class_name or 'GlobalNodeSet' in class_name or 'EphemeralNodeSet' in class_name or 'EphemeralEdgeSet' in class_name:
                for method in cls.findall('method'):
                    method_name = method.get('name')
                    for counter in method.findall('counter'):
                        if counter.get('type') == 'INSTRUCTION':
                            missed = int(counter.get('missed'))
                            covered = int(counter.get('covered'))
                            total = missed + covered
                            if missed > 0:
                                print(f"{class_name}::{method_name} Missed: {missed}/{total}")

if __name__ == '__main__':
    for f in glob.glob("**/jacocoTestReport.xml", recursive=True):
        parse_report(f)
