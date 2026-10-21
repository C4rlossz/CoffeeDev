#!/usr/bin/env python3
"""Reproducible project checks. Not a legal or accessibility certification."""
from pathlib import Path
import hashlib,json,re,math,sys
p=Path(__file__).resolve().parents[1];ui=p/'app/src/main/java/dev/coffeedev/ui'
report={'scope':'CoffeeDev local portfolio source','version':'1.2.0','checks':[],'contrast':[]}
def check(name, condition):
 report['checks'].append({'name':name,'passed':bool(condition)})
 check_result='PASS' if condition else 'FAIL'
 print(check_result,name)
strings=(ui/'Strings.kt').read_text()
pairs=re.findall(r'"([^"\n]+)" to listOf\(([^\n]+)\),?',strings)
check('all translation entries have three languages',all(len(re.findall(r'"(?:[^"\\]|\\.)*"',body))==3 for _,body in pairs))
check('no duplicate translation keys',len([k for k,_ in pairs])==len(set(k for k,_ in pairs)))
check('public legal entry before authentication','IconButton(onClick = openLegal)' in (ui/'CoffeeApp.kt').read_text())
check('local manifest has no Internet permission','android.permission.INTERNET' not in (p/'app/src/main/AndroidManifest.xml').read_text())
check('cloud explicitly declares Internet permission','android.permission.INTERNET' in (p/'app/src/cloud/AndroidManifest.xml').read_text())
check('backup disabled','android:allowBackup="false"' in (p/'app/src/main/AndroidManifest.xml').read_text())
for language in ['pt','en','es']:
 for name in ['privacy','terms','cookies','accessibility','credits']:
  f=p/f'app/src/main/assets/legal/{language}/{name}.txt'
  check(f'{language}/{name} bundled',f.exists() and f.stat().st_size>300)
login=(ui/'CoffeeApp.kt').read_text().split('@Composable private fun Login')[1].split('@Composable private fun Waiting')[0]
check('demo credentials absent from login UI','carlos@' not in login and 'CoffeeDev2026!' not in login)
check('registration has explicit terms action','readTerms' in login and 'accepted' in login)
check('form never auto-submits data','uriHandler.openUri(AUTHOR_CONTACT)' in (ui/'LegalCenter.kt').read_text() and '?text=' not in (ui/'LegalCenter.kt').read_text())
theme=(ui/'Theme.kt').read_text()
def lum(c):
 vals=[int(c[i:i+2],16)/255 for i in [0,2,4]]
 return sum((v/12.92 if v<=.04045 else ((v+.055)/1.055)**2.4)*w for v,w in zip(vals,[.2126,.7152,.0722]))
def ratio(a,b):
 a,b=sorted([lum(a),lum(b)]);return (b+.05)/(a+.05)
roles=['Primary','Secondary','Tertiary','PrimaryContainer','SecondaryContainer','TertiaryContainer','Background','Surface','SurfaceVariant','Error','ErrorContainer']
for name in ['Light','Dark']:
 block=theme.split(f'internal val {name} = ')[1].split('\n)')[0]
 colors=dict(re.findall(r'(\w+)\s*=\s*Color\(0xFF([0-9A-Fa-f]{6})\)',block))
 for key in re.findall(r'(\w+)\s*=\s*Color.White',block):colors[key]='FFFFFF'
 for role in roles:
  a=colors['on'+role];b=colors[role[0].lower()+role[1:]];r=ratio(a,b)
  report['contrast'].append({'theme':name,'foreground':'on'+role,'background':role,'ratio':r,'minimum':4.5,'passed':r>=4.5})
 for bg in ['surface','background','primaryContainer','surfaceVariant']:
  r=ratio(colors['outline'],colors[bg]);report['contrast'].append({'theme':name,'foreground':'outline','background':bg,'ratio':r,'minimum':3,'passed':r>=3})
check('declared color roles meet text/outline thresholds',all(x['passed'] for x in report['contrast']))
images=[]
for f in sorted((p/'app/src/main/res/drawable-nodpi').glob('photo_*.webp')):
 images.append({'path':str(f.relative_to(p)),'sha256':hashlib.sha256(f.read_bytes()).hexdigest(),'bytes':f.stat().st_size,'origin':'OpenAI image generation; concept food image','visible_logos_people_watermarks':False,'review':'Visual review of all ten images on 2026-10-21; not an exhaustive similarity search'})
check('ten local food images',len(images)==10)
check('no Firebase client configuration included',not (p/'app/google-services.json').exists())
report['limitations']=['Static source checks do not certify legal compliance.','Contrast roles are checked, not every rendered pixel/state.','No manual TalkBack/device certification.','No worldwide trademark/reverse-image clearance.']
(p/'docs/auditoria/checagens.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
(p/'docs/auditoria/imagens.json').write_text(json.dumps(images,ensure_ascii=False,indent=2)+'\n')
sys.exit(0 if all(x['passed'] for x in report['checks']) else 1)
