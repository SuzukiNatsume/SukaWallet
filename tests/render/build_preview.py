from pathlib import Path
import shutil,re
import tempfile
root=Path(__file__).resolve().parents[2]
dst=Path(tempfile.gettempdir())/'sukawallet-rendercheck'
dst.mkdir(exist_ok=True)
if (dst/'app').exists():shutil.rmtree(dst/'app')
shutil.copytree(root/'app',dst/'app');shutil.copy2(root/'build.sh',dst/'build.sh')
(dst/'app/src/main/java/top/suzuki/sukawallet/RenderChecks.java').write_text((Path(__file__).parent/'RenderChecks.java').read_text())
p=dst/'app/src/main/AndroidManifest.xml';s=p.read_text().replace('package="top.suzuki.sukawallet"','package="top.suzuki.sukawallet.rendercheck"').replace('android:name=".','android:name="top.suzuki.sukawallet.');s=s.replace('<application ','<application android:debuggable="true" ',1);s=re.sub(r'            <intent-filter>.*?</intent-filter>\n','',s,flags=re.S);s=s.replace('</manifest>','<instrumentation android:name="top.suzuki.sukawallet.RenderChecks" android:targetPackage="top.suzuki.sukawallet.rendercheck" /></manifest>');p.write_text(s)
p=dst/'build.sh';s=p.read_text().replace('--java build/generated','--custom-package top.suzuki.sukawallet --java build/generated');p.write_text(s)
p=dst/'app/src/main/java/top/suzuki/sukawallet/WalletApp.java';s=p.read_text().replace('Migration.importOnce(this);','');p.write_text(s)

print(dst)
