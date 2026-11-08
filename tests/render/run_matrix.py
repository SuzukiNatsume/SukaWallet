import subprocess,time,json,re
from pathlib import Path
out=Path(__file__).resolve().parent/'results';out.mkdir(exist_ok=True)
pkg='top.suzuki.sukawallet.rendercheck';runner=pkg+'/top.suzuki.sukawallet.RenderChecks'
def adb(*a):return subprocess.check_output(['adb',*a],text=True)
results=[]
density_state=adb('shell','wm','density')
original_density=re.search(r'Override density: (\d+)',density_state)
original_font=adb('shell','settings','get','system','font_scale').strip()
keys=['window_animation_scale','transition_animation_scale','animator_duration_scale']
original={key:adb('shell','settings','get','global',key).strip() for key in keys}
def run(dpi,scale,lang='en-US'):
 adb('shell','settings','put','system','font_scale',str(scale));time.sleep(.7)
 capture=(scale==.9 or (dpi==480 and scale==2.0))
 raw=adb('shell','am','instrument','-w','-e','capture',str(capture).lower(),runner)
 name=f'{dpi}-{scale}-{lang}';(out/(name+'.txt')).write_text(raw)
 fields={k:v for k,v in re.findall(r'^INSTRUMENTATION_RESULT: ([^=]+)=(.*)$',raw,re.M)}
 item={'dpi':dpi,'font':scale,'locale':lang,'count':int(fields.get('count','0')),'errors':json.loads(fields.get('failures','[]')),'reportedDpi':fields.get('dpi'),'reportedFont':fields.get('font'),'exception':fields.get('exception')}
 if str(dpi)!=fields.get('dpi') or abs(float(fields.get('font','0'))-scale)>.001:item['configuration_mismatch']=True
 results.append(item);(out/'results.json').write_text(json.dumps(results,ensure_ascii=False,indent=2))
 print(json.dumps({k:v for k,v in item.items() if k!='errors'} | {'errors':len(item['errors'])}),flush=True)
 if capture:
  for style in ['miuix','material']:
   for page in ['MainActivity','SettingsActivity']:
    with (out/(name+'-'+style+'-'+page+'.png')).open('wb') as f:
     subprocess.run(['adb','exec-out','run-as',pkg,'cat','files/'+style+'_'+page+'.png'],stdout=f,check=True)
try:
 for key in keys:adb('shell','settings','put','global',key,'0')
 adb('shell','cmd','locale','set-app-locales',pkg,'--locales','en-US')
 for dpi in [320,384,420,480]:
  adb('shell','wm','density',str(dpi));time.sleep(1.5)
  for scale in [.85,.9,1.0,1.1,1.15,1.25,1.3,1.5,1.8,2.0]:run(dpi,scale)
 for lang in ['zh-CN','zh-TW']:
  adb('shell','cmd','locale','set-app-locales',pkg,'--locales',lang);run(480,2.0,lang)
finally:
 if original_density:adb('shell','wm','density',original_density.group(1))
 else:adb('shell','wm','density','reset')
 adb('shell','settings','put','system','font_scale',original_font);adb('shell','cmd','locale','set-app-locales',pkg);time.sleep(1)
 for key,value in original.items():
  if value=='null':adb('shell','settings','delete','global',key)
  else:adb('shell','settings','put','global',key,value)
 print('Restored original density, font scale, and animation preferences',flush=True)
