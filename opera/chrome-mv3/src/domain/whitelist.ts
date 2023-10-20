import { Settings } from "./settings";

class Whitelist {
  private storage: Settings;

  constructor() {
    this.storage = Settings.getInstance();
  }

  async isWhitelisted(host: string): Promise<boolean> {
    if (!host) {
      return false;
    }

    const whitelist = await this.getList();
    return whitelist.includes(host);
  }

  async add(host: string): Promise<void> {
    if (await this.isWhitelisted(host)) {
      return;
    }

    const list = await this.getList();
    list.push(host);
    console.log(list);
    this.storage.setWhitelist(list);
  }

  async remove(host: string): Promise<void> {
    if (!(await this.isWhitelisted(host))) {
      return;
    }

    let list = await this.getList();
    list = list.filter((item) => item !== host);
    console.log(list);
    this.storage.setWhitelist(list);
  }

  async update(host: string): Promise<boolean> {
    if (await this.isWhitelisted(host)) {
      // Host is already whitelisted, so remove it
      await this.remove(host);
      return false;
    } else {
      // Host is not whitelisted, so add it
      await this.add(host);
      return true;
    }
  }

  private async getList(): Promise<string[]> {
    return await this.storage.getWhitelist();
  }
}

export { Whitelist };
