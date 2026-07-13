import xml.etree.ElementTree as ET
import sys
import glob

def parse_report(file_path):
    tree = ET.parse(file_path)
    root = tree.getroot()

    print(f"\nReport: {file_path}")
    print(f"{'Class':<50} {'Missed/Total':<15} {'Coverage %':<10}")
    print("-" * 75)

    for package in root.findall('package'):
        for cls in package.findall('class'):
            class_name = cls.get('name')
            for counter in cls.findall('counter'):
                if counter.get('type') == 'INSTRUCTION':
                    missed = int(counter.get('missed'))
                    covered = int(counter.get('covered'))
                    total = missed + covered
                    if total > 0:
                        coverage = (covered / total) * 100
                        if coverage < 100:
                            print(f"{class_name:<50} {missed}/{total:<10} {coverage:.2f}%")

if __name__ == '__main__':
    for f in glob.glob("**/jacocoTestReport.xml", recursive=True):
        parse_report(f)
